package net.opencraft.client.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.opencraft.client.OpenCraft;
import net.opencraft.shared.network.INetworkManager;
import net.opencraft.shared.network.PacketManager;
import net.opencraft.shared.network.packets.IPacket;
import net.opencraft.shared.network.packets.PacketBlockChange;
import net.opencraft.shared.network.packets.PacketPlayerInfo;
import net.opencraft.shared.network.packets.PacketPlayerPosition;

/**
 * Client-side network manager for handling communication with servers
 */
public class ClientNetworkManager implements INetworkManager {

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Queue<Object> receivedPackets;
    private boolean connectionOpen;
    private boolean connectedToIntegratedServer = false;
    private OpenCraft gameInstance;

    // For integrated server communication
    private net.opencraft.shared.network.LocalNetworkManager localNetworkManager;

    // For remote connections
    private String serverAddress;
    private int serverPort;

    // For receiving packets
    private ExecutorService packetProcessor;
    private volatile boolean running = false;

    private byte[] pendingPacketData;
    private int bytesReceived = 0;
    private int expectedLength = -1;

    private net.opencraft.shared.network.ConnectionState connectionState = net.opencraft.shared.network.ConnectionState.HANDSHAKING;

    // Network statistics for optimization
    private long lastPingTime = 0;
    private int ping = 0;
    private long lastPacketReceivedTime = 0;

    public ClientNetworkManager(OpenCraft gameInstance) {
        this.gameInstance = gameInstance;
        this.receivedPackets = new ConcurrentLinkedQueue<>();
        this.packetProcessor = Executors.newSingleThreadExecutor();
        this.connectionOpen = false;
        this.lastPacketReceivedTime = System.currentTimeMillis();
    }

    public void setConnectionState(net.opencraft.shared.network.ConnectionState state) {
        this.connectionState = state;
    }

    public net.opencraft.shared.network.ConnectionState getConnectionState() {
        return this.connectionState;
    }

    /**
     * Connect to an integrated server (running in the same process)
     */
    public void connectToIntegratedServer() {
        // Get the server instance and its network system
        net.opencraft.server.Server integratedServer = gameInstance.integratedServer;
        if (integratedServer != null && integratedServer.getNetworkSystem() != null) {
            // Get the local network manager from the server
            this.localNetworkManager = integratedServer.getNetworkSystem().getLocalNetworkManager();
            connectedToIntegratedServer = true;
            connectionOpen = true;

            // Integrated server doesn't use the state machine strictly for serialization,
            // but we should still track it.
            this.connectionState = net.opencraft.shared.network.ConnectionState.PLAY; // Or whatever

            // Add a player to the server
            integratedServer.addPlayer("SinglePlayer", "singleplayer-uuid");

            System.out.println("Connected to integrated server");
        } else {
            System.err.println("Failed to connect to integrated server: server not available");
        }
    }

    /**
     * Connect to a remote server
     */
    public boolean connectToRemoteServer(String address, int port) {
        try {
            socket = new Socket(address, port);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            serverAddress = address;
            serverPort = port;
            connectionOpen = true;
            running = true;

            // Reset state
            this.connectionState = net.opencraft.shared.network.ConnectionState.HANDSHAKING;

            // Start receiving packets in a separate thread
            packetProcessor.submit(this::receivePackets);

            System.out.println("Connected to remote server: " + address + ":" + port);

            // Send Handshake
            sendHandshake(address, port);

            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            connectionOpen = false;
            return false;
        }
    }

    private void sendHandshake(String address, int port) throws IOException {
        // Protocol version 1, Next State 2 (Login), Username
        String username = (gameInstance.sessionData != null) ? gameInstance.sessionData.username
                : "Player" + System.currentTimeMillis() % 1000;
        net.opencraft.shared.network.packets.PacketHandshake handshake = new net.opencraft.shared.network.packets.PacketHandshake(
                1, 2, username);
        sendPacket(handshake);
        // State remains HANDSHAKING until we receive LoginSuccess?
        // Actually, after sending Handshake (which indicates next state Login), we
        // should switch to LOGIN state?
        // But we wait for LoginSuccess. PacketManager needs to be able to deserialize
        // LoginSuccess.
        // LoginSuccess is in LOGIN state.
        // So we should switch to LOGIN state immediately after sending Handshake?
        // Or does the server send LoginSuccess in response?
        // Yes. The server receives Handshake, switches to Login, sends LoginSuccess.
        // The client must be in LOGIN state to receive LoginSuccess.
        setConnectionState(net.opencraft.shared.network.ConnectionState.LOGIN);
    }

    @Override
    public void sendPacket(Object packet) throws IOException {
        if (!connectionOpen) {
            throw new IOException("Not connected to server");
        }

        if (packet instanceof IPacket) {
            if (connectedToIntegratedServer && localNetworkManager != null) {
                // For integrated server, use the local network manager
                localNetworkManager.sendPacket(packet);
            } else if (!connectedToIntegratedServer) {
                // For remote server, serialize and send via socket
                byte[] serializedPacket = PacketManager.serializePacket((IPacket) packet);
                outputStream.writeInt(serializedPacket.length);
                outputStream.write(serializedPacket);
                outputStream.flush();
            }
        }
    }

    @Override
    public boolean isConnectionOpen() {
        if (connectedToIntegratedServer) {
            return connectionOpen;
        } else {
            return connectionOpen && socket != null && !socket.isClosed() && socket.isConnected();
        }
    }

    @Override
    public void closeConnection() {
        running = false;
        connectionOpen = false;

        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }

        packetProcessor.shutdown();
    }

    @Override
    public Object getReceivedPacket() {
        if (connectedToIntegratedServer && localNetworkManager != null) {
            // For integrated server, get packets from local network manager
            return localNetworkManager.getReceivedPacket();
        } else {
            // For remote server, get from our own queue
            return receivedPackets.poll();
        }
    }

    @Override
    public void processNetworkData() {
        // Process received packets
        Object packet;
        if (connectedToIntegratedServer && localNetworkManager != null) {
            // For integrated server, process packets directly from the local network
            // manager
            while ((packet = localNetworkManager.getReceivedPacket()) != null) {
                processReceivedPacket(packet);
            }
        } else {
            // For remote server, process from our queue
            while ((packet = receivedPackets.poll()) != null) {
                processReceivedPacket(packet);
            }

            // Check connection health
            checkConnectionHealth();
        }
    }

    @Override
    public boolean isLocal() {
        // TODO: idk what to return here
        return false;
    }

    /**
     * Check the health of the connection
     */
    private void checkConnectionHealth() {
        if (!connectedToIntegratedServer && System.currentTimeMillis() - lastPacketReceivedTime > 30000) { // 30 seconds
            // No packets received in 30 seconds, connection might be dead
            System.out.println("Connection timeout: No packets received for 30 seconds");
            connectionOpen = false;
        }
    }

    /**
     * Get the connection ping in milliseconds
     */
    public int getPing() {
        return ping;
    }

    /**
     * Check if connection is healthy
     */
    public boolean isConnectionHealthy() {
        long timeSinceLastPacket = System.currentTimeMillis() - lastPacketReceivedTime;
        return timeSinceLastPacket < 10000; // Healthy if received packet in last 10 seconds
    }

    /**
     * Process a received packet
     */
    /**
     * Process a received packet
     */
    private void processReceivedPacket(Object packetObj) {
        if (!(packetObj instanceof IPacket)) {
            return;
        }

        IPacket packet = (IPacket) packetObj;

        // Handle packets based on type (instanceof is safest as IDs overlap across
        // states)
        if (packet instanceof net.opencraft.shared.network.packets.PacketLoginSuccess) {
            handleLoginSuccess((net.opencraft.shared.network.packets.PacketLoginSuccess) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketRegistryData) {
            handleRegistryData((net.opencraft.shared.network.packets.PacketRegistryData) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketFinishConfig) {
            handleFinishConfig((net.opencraft.shared.network.packets.PacketFinishConfig) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketKeepAlive) {
            // TODO: Respond to KeepAlive
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketChatMessage) {
            handleChatMessage((net.opencraft.shared.network.packets.PacketChatMessage) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketChunkData) {
            handleChunkDataPacket((net.opencraft.shared.network.packets.PacketChunkData) packet);
        } else if (packet instanceof PacketBlockChange) {
            handleBlockChangePacket((PacketBlockChange) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketSpawnEntity) {
            handleSpawnEntity((net.opencraft.shared.network.packets.PacketSpawnEntity) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketEntityPosition) {
            handleEntityPosition((net.opencraft.shared.network.packets.PacketEntityPosition) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketEntityRelativeMove) {
            handleEntityRelativeMove((net.opencraft.shared.network.packets.PacketEntityRelativeMove) packet);
        } else if (packet instanceof PacketPlayerPosition) {
            // Legacy support or if we re-introduce it
            handlePlayerPositionPacket((PacketPlayerPosition) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketWorldInit) {
            handleWorldInitPacket((net.opencraft.shared.network.packets.PacketWorldInit) packet);
        } else if (packet instanceof PacketPlayerInfo) {
            handlePlayerInfoPacket((PacketPlayerInfo) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketEffect) {
            handleEffectPacket((net.opencraft.shared.network.packets.PacketEffect) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketEntityState) {
            handleEntityStatePacket((net.opencraft.shared.network.packets.PacketEntityState) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketWorldState) {
            handleWorldStatePacket((net.opencraft.shared.network.packets.PacketWorldState) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketPlayerSpawn) {
            handlePlayerSpawnPacket((net.opencraft.shared.network.packets.PacketPlayerSpawn) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketWindowItems) {
            handleWindowItems((net.opencraft.shared.network.packets.PacketWindowItems) packet);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketSetSlot) {
            handleSetSlot((net.opencraft.shared.network.packets.PacketSetSlot) packet);
        } else {
            System.out.println("Unknown packet type: " + packet.getClass().getSimpleName());
        }
    }

    private void handleWindowItems(net.opencraft.shared.network.packets.PacketWindowItems packet) {
        if (gameInstance == null || gameInstance.player == null)
            return;
        if (packet.getWindowId() == 0) {
            net.opencraft.core.item.ItemStack[] items = packet.getItems();
            net.opencraft.core.inventory.InventoryPlayer inventory = gameInstance.player.inventory;

            // 5-8: Armor
            for (int i = 0; i < 4; i++) {
                if (5 + i < items.length)
                    inventory.armorInventory[i] = items[5 + i];
            }
            // 9-35: Main Inventory (storage)
            for (int i = 9; i < 36; i++) {
                if (i < items.length)
                    inventory.mainInventory[i] = items[i];
            }
            // 36-44: Hotbar
            for (int i = 0; i < 9; i++) {
                if (36 + i < items.length)
                    inventory.mainInventory[i] = items[36 + i];
            }
        }
    }

    private void handleSetSlot(net.opencraft.shared.network.packets.PacketSetSlot packet) {
        if (gameInstance == null || gameInstance.player == null)
            return;
        if (packet.getWindowId() == 0) {
            int slot = packet.getSlot();
            net.opencraft.core.item.ItemStack item = packet.getItem();
            net.opencraft.core.inventory.InventoryPlayer inventory = gameInstance.player.inventory;

            if (slot == -1) {
                inventory.setCursorItem(item);
            } else if (slot >= 5 && slot < 9) {
                inventory.armorInventory[slot - 5] = item;
            } else if (slot >= 9 && slot < 36) {
                inventory.mainInventory[slot] = item;
            } else if (slot >= 36 && slot < 45) {
                inventory.mainInventory[slot - 36] = item;
            }
        }
    }

    private void handleLoginSuccess(net.opencraft.shared.network.packets.PacketLoginSuccess packet) {
        System.out.println("Login Success! Username: " + packet.getUsername());
        setConnectionState(net.opencraft.shared.network.ConnectionState.CONFIGURATION);
    }

    private void handleRegistryData(net.opencraft.shared.network.packets.PacketRegistryData packet) {
        System.out.println("Received Registry Data for " + packet.getRegistryCode() + ": " + packet.getEntries().size()
                + " entries");
    }

    private void handleFinishConfig(net.opencraft.shared.network.packets.PacketFinishConfig packet) {
        System.out.println("Received FinishConfig. Sending AckFinish and switching to PLAY.");
        try {
            sendPacket(new net.opencraft.shared.network.packets.PacketAckFinish());
            setConnectionState(net.opencraft.shared.network.ConnectionState.PLAY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleChatMessage(net.opencraft.shared.network.packets.PacketChatMessage packet) {
        System.out.println("CHAT: " + packet.getMessage());
        if (gameInstance != null && gameInstance.ingameGUI != null) {
            gameInstance.ingameGUI.addChatMessage(packet.getMessage());
        }
    }

    private void handleSpawnEntity(net.opencraft.shared.network.packets.PacketSpawnEntity packet) {
        System.out.println("Spawn Entity: " + packet.getType() + " (ID: " + packet.getEntityId() + ") at "
                + packet.getX() + ", " + packet.getY() + ", " + packet.getZ());
        // TODO: Spawn actual entity in world
    }

    private void handleEntityPosition(net.opencraft.shared.network.packets.PacketEntityPosition packet) {
        // Update entity position
    }

    private void handleEntityRelativeMove(net.opencraft.shared.network.packets.PacketEntityRelativeMove packet) {
        // Update entity relative move
    }

    /**
     * Handle player position packet (server reconciliation for hybrid prediction)
     */
    private void handlePlayerPositionPacket(PacketPlayerPosition packet) {
        System.out.println("CLIENT: handlePlayerPositionPacket called - packet pos: (" +
                packet.getX() + ", " + packet.getY() + ", " + packet.getZ() + ")");

        if (gameInstance == null || gameInstance.player == null) {
            System.out.println("CLIENT: Skipping - gameInstance or player is null");
            return;
        }

        System.out.println("CLIENT: Current player pos: (" +
                gameInstance.player.posX + ", " + gameInstance.player.posY + ", " + gameInstance.player.posZ + ")");

        // For multiplayer worlds, spawn the player on first position update from server
        // This ensures the player entity is added to the world with the correct
        // position
        if (gameInstance.isMultiplayerWorld() && gameInstance.clientWorld != null) {
            System.out.println("CLIENT: isMultiplayerWorld=true, checking if player needs spawning");
            // Check if player needs to be spawned (not yet in world's entity list)
            boolean playerInWorld = gameInstance.clientWorld.getLoadedEntityList().contains(gameInstance.player);
            System.out.println("CLIENT: Player in world entity list: " + playerInWorld);

            if (!playerInWorld) {
                // Set position first, then spawn
                // Packet contains Eye Position, but setPositionAndRotation expects Feet
                // Position (adds yOffset)
                // So we subtract yOffset here to cancel it out
                gameInstance.player.setPositionAndRotation(
                        packet.getX(),
                        packet.getY() - gameInstance.player.yOffset,
                        packet.getZ(),
                        packet.getYaw(),
                        packet.getPitch());

                // Now spawn the player with the correct position
                gameInstance.clientWorld.spawnPlayerWithLoadedChunks();

                // Mark player as spawned so it can start sending position updates
                if (gameInstance.player instanceof net.opencraft.client.entity.EntityPlayerSP) {
                    ((net.opencraft.client.entity.EntityPlayerSP) gameInstance.player).hasSpawnedInWorld = true;
                    System.out.println("CLIENT: Set hasSpawnedInWorld = true");
                }

                System.out.println("Spawned player at server position: (" +
                        String.format("%.2f", packet.getX()) + ", " +
                        String.format("%.2f", packet.getY()) + ", " +
                        String.format("%.2f", packet.getZ()) + ")");
                return; // Skip reconciliation on first spawn
            } else {
                // Player already in world, just set the flag if not already set
                if (gameInstance.player instanceof net.opencraft.client.entity.EntityPlayerSP) {
                    net.opencraft.client.entity.EntityPlayerSP playerSP = (net.opencraft.client.entity.EntityPlayerSP) gameInstance.player;
                    if (!playerSP.hasSpawnedInWorld) {
                        playerSP.hasSpawnedInWorld = true;
                        System.out.println("CLIENT: Player already in world, set hasSpawnedInWorld = true");
                    }
                }
            }
        }

        // Calculate error between client prediction and server position
        double errorX = packet.getX() - gameInstance.player.posX;
        double errorY = packet.getY() - gameInstance.player.posY;
        double errorZ = packet.getZ() - gameInstance.player.posZ;

        double errorMagnitude = Math.sqrt(errorX * errorX + errorY * errorY + errorZ * errorZ);

        // Only reconcile if error is significant (> 0.1 blocks)
        // Small errors are ignored to prevent jitter from network latency
        if (errorMagnitude > 0.1) {
            // If the error is large (e.g. teleport), snap immediately
            if (errorMagnitude > 8.0) {
                gameInstance.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
                gameInstance.player.motionX = 0;
                gameInstance.player.motionY = 0;
                gameInstance.player.motionZ = 0;
                System.out.println("Server reconciliation: large error (" + String.format("%.3f", errorMagnitude) +
                        "), snapping to position");
            } else {
                // Otherwise, smooth correction
                ((net.opencraft.client.entity.EntityPlayerSP) gameInstance.player).setPositionSmooth(
                        packet.getX(),
                        packet.getY(),
                        packet.getZ(),
                        packet.getYaw(),
                        packet.getPitch());

                System.out.println("Server reconciliation: error=" + String.format("%.3f", errorMagnitude) +
                        " blocks, smoothly correcting position");
            }
        }

        // Always trust server for on-ground state
        gameInstance.player.onGround = packet.isOnGround();
    }

    /**
     * Handle block change packet
     */
    private void handleBlockChangePacket(PacketBlockChange packet) {
        // Update block in the client world
        if (gameInstance != null && gameInstance.clientWorld != null) {
            System.out.println("Received block change: (" +
                    packet.getX() + ", " + packet.getY() + ", " + packet.getZ() +
                    ") -> Block ID: " + packet.getBlockId());

            // Actually update the world
            gameInstance.clientWorld.setBlockWithNotify(packet.getX(), packet.getY(), packet.getZ(),
                    packet.getBlockId());
        }
    }

    /**
     * Handle world initialization packet
     */
    private void handleWorldInitPacket(net.opencraft.shared.network.packets.PacketWorldInit packet) {
        // Update client world with server information
        System.out.println("Received world init from server: " + packet.getWorldName() +
                " at spawn (" + packet.getSpawnX() + ", " + packet.getSpawnY() + ", " + packet.getSpawnZ() + ")");

        if (gameInstance != null && gameInstance.clientWorld != null) {
            // Update world spawn coordinates
            gameInstance.clientWorld.x = packet.getSpawnX();
            gameInstance.clientWorld.y = packet.getSpawnY();
            gameInstance.clientWorld.z = packet.getSpawnZ();

            // CRITICAL: Set the player's position to the spawn coordinates
            // This prevents the violent shaking/glitching caused by position desync
            if (gameInstance.player != null) {
                // Use setPositionAndRotation to handle yOffset correctly (expects Feet
                // Position)
                gameInstance.player.setPositionAndRotation(
                        packet.getSpawnX() + 0.5, // Center of block
                        packet.getSpawnY(), // Feet position
                        packet.getSpawnZ() + 0.5, // Center of block
                        0.0f, 0.0f);
                System.out.println("Set player position to spawn: (" +
                        packet.getSpawnX() + ", " + packet.getSpawnY() + ", " + packet.getSpawnZ() + ")");
            }
        }
    }

    /**
     * Handle chunk data packet
     */
    private void handleChunkDataPacket(net.opencraft.shared.network.packets.PacketChunkData packet) {
        if (gameInstance != null && gameInstance.clientWorld != null) {
            // Get the chunk provider and load the chunk data
            net.opencraft.client.world.IChunkProvider chunkProvider = gameInstance.clientWorld.getChunkProvider();
            if (chunkProvider instanceof net.opencraft.client.world.chunk.ChunkProviderClient) {
                ((net.opencraft.client.world.chunk.ChunkProviderClient) chunkProvider).loadChunkFromPacket(
                        packet.getChunkX(),
                        packet.getChunkZ(),
                        packet.getData());
            } else {
                System.err.println("Warning: Client world is not using ChunkProviderClient!");
            }
        }
    }

    /**
     * Handle player info packet
     */
    private void handlePlayerInfoPacket(PacketPlayerInfo packet) {
        // Handle player join/leave notifications
        if (packet.isJoining()) {
            System.out.println("Player joined: " + packet.getPlayerName());
        } else {
            System.out.println("Player left: " + packet.getPlayerName());
        }
    }

    /**
     * Handle effect packet (particles and sounds)
     */
    private void handleEffectPacket(net.opencraft.shared.network.packets.PacketEffect packet) {
        if (gameInstance == null || gameInstance.clientWorld == null) {
            return;
        }

        net.opencraft.shared.network.EffectType effectType = packet.getEffectType();

        if (effectType.isParticle()) {
            // Spawn particle effect
            String particleName = getParticleNameFromType(effectType);
            gameInstance.clientWorld.spawnParticle(
                    particleName,
                    packet.getX(),
                    packet.getY(),
                    packet.getZ(),
                    packet.getVelocityX(),
                    packet.getVelocityY(),
                    packet.getVelocityZ());
        } else if (effectType.isSound()) {
            // Play sound effect
            String soundName = getSoundNameFromType(effectType);
            gameInstance.clientWorld.playSoundEffect(
                    packet.getX(),
                    packet.getY(),
                    packet.getZ(),
                    soundName,
                    packet.getVolume(),
                    packet.getPitch());
        }
    }

    /**
     * Handle entity state packet
     */
    private void handleEntityStatePacket(net.opencraft.shared.network.packets.PacketEntityState packet) {
        // TODO: Implement entity state handling
        // This would update client-side entity positions, rotations, and velocities
        // For now, just log the number of entities received
        System.out.println("Received entity state update for " + packet.getEntities().size() + " entities");

        // In a full implementation, this would:
        // 1. Iterate through all entities in the packet
        // 2. Find or create corresponding client-side entities
        // 3. Update their positions, rotations, velocities, and health
        // 4. Apply interpolation for smooth movement
    }

    /**
     * Handle world state packet
     */
    private void handleWorldStatePacket(net.opencraft.shared.network.packets.PacketWorldState packet) {
        if (gameInstance == null || gameInstance.clientWorld == null) {
            return;
        }

        // Update client world state from server
        // TODO: Add setWorldTime method to ClientWorld
        // gameInstance.clientWorld.setWorldTime(packet.getWorldTime());

        // TODO: Update weather state when those fields are available in ClientWorld
        // gameInstance.clientWorld.setRaining(packet.isRaining());
        // gameInstance.clientWorld.setRainStrength(packet.getRainStrength());
        // gameInstance.clientWorld.setThundering(packet.isThundering());
        // gameInstance.clientWorld.setThunderStrength(packet.getThunderStrength());

        System.out.println("World state updated: time=" + packet.getWorldTime());
    }

    /**
     * Handle player spawn packet
     */
    private void handlePlayerSpawnPacket(net.opencraft.shared.network.packets.PacketPlayerSpawn packet) {
        System.out.println("Received player spawn packet: " + packet.getUsername() +
                " at (" + packet.getX() + ", " + packet.getY() + ", " + packet.getZ() + ")");

        if (gameInstance == null)
            return;

        // Check if this is for the local player
        // In singleplayer/integrated, we assume it is if we don't have a player yet, or
        // if names match
        boolean isLocalPlayer = gameInstance.sessionData != null &&
                (gameInstance.sessionData.username.equals(packet.getUsername()) ||
                        "SinglePlayer".equals(packet.getUsername()));

        if (isLocalPlayer) {
            if (gameInstance.player == null) {
                System.out.println("Initializing local player from spawn packet");
                // Create the player entity
                net.opencraft.client.entity.EntityPlayerSP player = new net.opencraft.client.entity.EntityPlayerSP(
                        gameInstance, gameInstance.clientWorld, gameInstance.sessionData);

                // Set position from packet
                player.setPositionAndRotation(
                        packet.getX(),
                        packet.getY(),
                        packet.getZ(),
                        packet.getYaw(),
                        packet.getPitch());

                // Initialize player
                player.preparePlayerToSpawn();
                player.hasSpawnedInWorld = true;

                // Set game instance player
                gameInstance.player = player;

                // Add to world if world exists
                if (gameInstance.clientWorld != null) {
                    gameInstance.clientWorld.player = player;
                    gameInstance.clientWorld.spawnPlayerWithLoadedChunks();
                }

                // Initialize controller
                if (gameInstance.playerController != null) {
                    gameInstance.playerController.flipPlayer(player);
                    gameInstance.playerController.func_6473_b(player);
                }

                // Initialize movement input
                player.movementInput = new net.opencraft.client.input.MovementInput(
                        gameInstance.options, gameInstance.keyboard);
            } else {
                // Player already exists, just update position (teleport)
                System.out.println("Player already exists, updating position from spawn packet");
                gameInstance.player.setPositionAndRotation(
                        packet.getX(),
                        packet.getY(),
                        packet.getZ(),
                        packet.getYaw(),
                        packet.getPitch());
            }
        } else {
            // TODO: Handle other players (EntityOtherPlayerMP)
            System.out.println("Received spawn packet for other player: " + packet.getUsername());
        }
    }

    /**
     * Get particle name from effect type
     */
    private String getParticleNameFromType(net.opencraft.shared.network.EffectType effectType) {
        switch (effectType) {
            case PARTICLE_SMOKE:
                return "smoke";
            case PARTICLE_FIRE:
                return "flame";
            case PARTICLE_EXPLOSION:
                return "explode";
            case PARTICLE_SPLASH:
                return "splash";
            case PARTICLE_BUBBLE:
                return "bubble";
            case PARTICLE_LAVA:
                return "lava";
            case PARTICLE_LARGE_SMOKE:
                return "largesmoke";
            case PARTICLE_FLAME:
                return "flame";
            default:
                return "smoke";
        }
    }

    /**
     * Get sound name from effect type
     */
    private String getSoundNameFromType(net.opencraft.shared.network.EffectType effectType) {
        switch (effectType) {
            case SOUND_ZOMBIE_IDLE:
                return "mob.zombie.say";
            case SOUND_ZOMBIE_HURT:
                return "mob.zombie.hurt";
            case SOUND_ZOMBIE_DEATH:
                return "mob.zombie.death";
            case SOUND_SKELETON_IDLE:
                return "mob.skeleton.say";
            case SOUND_SKELETON_HURT:
                return "mob.skeleton.hurt";
            case SOUND_SKELETON_DEATH:
                return "mob.skeleton.death";
            case SOUND_SPIDER_IDLE:
                return "mob.spider.say";
            case SOUND_SPIDER_HURT:
                return "mob.spider.say";
            case SOUND_SPIDER_DEATH:
                return "mob.spider.death";
            case SOUND_FIZZ:
                return "random.fizz";
            case SOUND_EXPLOSION:
                return "random.explode";
            case SOUND_BLOCK_DIG:
                return "dig.stone";
            case SOUND_BLOCK_STEP:
                return "step.stone";
            default:
                return "random.click";
        }
    }

    /**
     * Receive packets from the server in a separate thread
     */
    private void receivePackets() {
        while (running && isConnectionOpen()) {
            try {
                if (inputStream.available() > 0) {
                    if (expectedLength == -1) {
                        // We need to read the packet length first
                        if (inputStream.available() >= 4) { // int is 4 bytes
                            expectedLength = inputStream.readInt();
                            pendingPacketData = new byte[expectedLength];
                            bytesReceived = 0;
                        }
                    }

                    if (expectedLength > 0 && bytesReceived < expectedLength) {
                        // Read as much data as available
                        int toRead = Math.min(expectedLength - bytesReceived, inputStream.available());
                        int read = inputStream.read(pendingPacketData, bytesReceived, toRead);

                        if (read > 0) {
                            bytesReceived += read;
                        }

                        if (bytesReceived == expectedLength) {
                            // Complete packet received
                            IPacket packet = PacketManager.deserializePacket(pendingPacketData, connectionState);
                            receivedPackets.add(packet);

                            // Update network statistics
                            lastPacketReceivedTime = System.currentTimeMillis();

                            // Reset for next packet
                            expectedLength = -1;
                            pendingPacketData = null;
                            bytesReceived = 0;
                        }
                    }
                }

                Thread.sleep(1); // Small delay to prevent excessive CPU usage
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error receiving packet: " + e.getMessage());
                }
                break; // Exit if there's an error
            }
        }
    }

    /**
     * Check if connected to integrated server
     */
    public boolean isConnectedToIntegrated() {
        return connectedToIntegratedServer;
    }

    /**
     * Get the server address
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Get the server port
     */
    public int getServerPort() {
        return serverPort;
    }
}