package net.infcraft.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.infcraft.server.world.ServerWorld;
import net.infcraft.server.Server;
import net.infcraft.shared.network.INetworkManager;
import net.infcraft.shared.network.LocalNetworkManager;
import net.infcraft.shared.network.RemoteNetworkManager;
import net.infcraft.shared.network.packets.*;

import net.infcraft.core.util.Mth;

/**
 * Network system for the integrated server
 * Handles both local (integrated) connections and remote connections
 */
public class NetworkSystem {

    private Server server;
    private ExecutorService networkThreadPool;
    private boolean isRunning;

    // For remote connections
    private ServerSocket serverSocket;
    private Future<?> connectionAcceptorTask;

    // Track connected clients
    private Map<Socket, INetworkManager> connectedClients;

    // Per-player tracking
    private Map<String, INetworkManager> playerNetworkManagers; // username -> network manager
    private Map<INetworkManager, net.infcraft.core.entity.EntityPlayer> networkManagerToPlayerMap; // network manager
                                                                                                    // -> player
    private Map<String, java.util.Set<Long>> playerLoadedChunks; // username -> set of loaded chunk keys
    private Map<String, java.util.Set<Integer>> playerKnownEntities; // username -> set of entity IDs the player knows
                                                                     // about

    // For integrated/local server communication
    private LocalNetworkManager localNetworkManager;

    /**
     * Helper class for chunk loading priority
     */
    private static class ChunkLoadRequest {
        final long chunkKey;
        final int chunkX;
        final int chunkZ;
        final int distanceSquared;

        ChunkLoadRequest(long chunkKey, int chunkX, int chunkZ, int distanceSquared) {
            this.chunkKey = chunkKey;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.distanceSquared = distanceSquared;
        }
    }

    public NetworkSystem(Server server) {
        this.server = server;
        this.networkThreadPool = Executors.newFixedThreadPool(4); // Thread pool for network operations
        this.isRunning = false;
        this.connectedClients = new ConcurrentHashMap<>();
        this.playerNetworkManagers = new ConcurrentHashMap<>();
        this.networkManagerToPlayerMap = new ConcurrentHashMap<>();
        this.playerLoadedChunks = new ConcurrentHashMap<>();
        this.playerKnownEntities = new ConcurrentHashMap<>();
    }

    /**
     * Initialize the integrated server network
     */
    public void initializeIntegratedServer() {
        isRunning = true;

        // For integrated server, we create a local network manager for communication
        // The client will connect to the server running in the same process
        this.localNetworkManager = new LocalNetworkManager();
        System.out.println("Integrated server network initialized");
    }

    /**
     * Get the local network manager for integrated server
     */
    public LocalNetworkManager getLocalNetworkManager() {
        return localNetworkManager;
    }

    /**
     * Start listening for remote connections
     */
    public void startRemoteServer(int port) {
        try {
            serverSocket = new ServerSocket(port);

            // Start accepting connections in a separate thread
            connectionAcceptorTask = networkThreadPool.submit(this::acceptConnections);

            System.out.println("Server listening on port " + port);
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
        }
    }

    /**
     * Accept incoming connections
     */
    private void acceptConnections() {
        while (isRunning && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleNewConnection(clientSocket);
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
                break; // Exit if there's an error and we're still supposed to be running
            }
        }
    }

    /**
     * Handle a new client connection
     */
    private void handleNewConnection(Socket clientSocket) throws IOException {
        // In a real implementation, we'd perform handshake here
        // For now, we'll just create a basic network manager for the client
        INetworkManager networkManager = new RemoteNetworkManager(clientSocket);
        connectedClients.put(clientSocket, networkManager);

        System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

        // Process client data asynchronously
        networkThreadPool.submit(() -> processClientData(clientSocket, networkManager));
    }

    /**
     * Process data from a connected client
     */
    private void processClientData(Socket clientSocket, INetworkManager networkManager) {
        while (isRunning && networkManager.isConnectionOpen()) {
            try {
                networkManager.processNetworkData();

                // Process any received packets
                Object packet;
                while ((packet = networkManager.getReceivedPacket()) != null) {
                    if (packet instanceof IPacket) {
                        processPacketFromClient((IPacket) packet, clientSocket);
                    }
                }

                // Small delay to prevent excessive CPU usage
                Thread.sleep(1);
            } catch (Exception e) {
                System.err.println("Error processing client data: " + e.getMessage());
                break;
            }
        }

        // Client disconnected, clean up
        connectedClients.remove(clientSocket);
    }

    /**
     * Process a packet from a specific client
     */
    private void processPacketFromClient(IPacket packet, Socket clientSocket) {
        // Handle the packet - in a real implementation, this would involve
        // processing different types of packets and updating the game state
        // based on the packet contents

        // We need to get the network manager for this client to update its state
        INetworkManager networkManager;
        if (clientSocket != null) {
            networkManager = connectedClients.get(clientSocket);
        } else {
            // Local client
            networkManager = localNetworkManager;
        }

        if (packet instanceof net.infcraft.shared.network.packets.PacketHandshake) {
            handleHandshake((net.infcraft.shared.network.packets.PacketHandshake) packet, networkManager);
        } else if (packet instanceof net.infcraft.shared.network.packets.PacketAckFinish) {
            handleAckFinish((net.infcraft.shared.network.packets.PacketAckFinish) packet, networkManager);
        } else if (packet instanceof net.infcraft.shared.network.packets.PacketKeepAlive) {
            // Respond to KeepAlive? Or just ignore for now.
        } else if (packet instanceof net.infcraft.shared.network.packets.PacketChatMessage) {
            // Handle chat
        } else if (packet instanceof net.infcraft.shared.network.packets.PacketPlayerPosition) {
            // Handle old-style player position packet

            // reconstruct
            handlePlayerPositionRotation(
                    new net.infcraft.shared.network.packets.PacketPlayerPositionRotation(
                            (float) ((PacketPlayerPosition) packet).getX(),
                            (float) ((PacketPlayerPosition) packet).getY(),
                            (float) ((PacketPlayerPosition) packet).getZ(),
                            ((net.infcraft.shared.network.packets.PacketPlayerPosition) packet).getYaw(),
                            ((net.infcraft.shared.network.packets.PacketPlayerPosition) packet).getPitch(),
                            ((net.infcraft.shared.network.packets.PacketPlayerPosition) packet).isOnGround()),
                    networkManager); // Pass the networkManager
        } else if (packet instanceof net.infcraft.shared.network.packets.PacketPlayerPositionRotation) {
            // Handle new-style player position packet
            handlePlayerPositionRotation(
                    (net.infcraft.shared.network.packets.PacketPlayerPositionRotation) packet,
                    networkManager);
        } else {
            // Existing handling
            switch (packet.getPacketId()) {
                case 0x22: // Player Digging
                    if (packet instanceof net.infcraft.shared.network.packets.PacketPlayerDigging) {
                        handlePlayerDigging((net.infcraft.shared.network.packets.PacketPlayerDigging) packet,
                                networkManager);
                    }
                    break;
                case 0x23: // Block Placement
                    if (packet instanceof net.infcraft.shared.network.packets.PacketBlockPlacement) {
                        handleBlockPlacement((net.infcraft.shared.network.packets.PacketBlockPlacement) packet,
                                networkManager);
                    }
                    break;
                case 0x24: // Held Item Change
                    if (packet instanceof net.infcraft.shared.network.packets.PacketHeldItemChange) {
                        handleHeldItemChange((net.infcraft.shared.network.packets.PacketHeldItemChange) packet,
                                networkManager);
                    }
                    break;
                case 0x60: // Window Click
                    if (packet instanceof net.infcraft.shared.network.packets.PacketWindowClick) {
                        handleWindowClick((net.infcraft.shared.network.packets.PacketWindowClick) packet,
                                networkManager);
                    }
                    break;
                default:
                    // System.out.println("Unhandled packet ID: 0x" +
                    // Integer.toHexString(packet.getPacketId()));
                    break;
            }
        }
    }

    private void handleWindowClick(net.infcraft.shared.network.packets.PacketWindowClick packet,
            INetworkManager networkManager) {
        if (server.getWorld() == null)
            return;

        net.infcraft.core.entity.EntityPlayer player = getPlayerFromNetworkManager(networkManager);
        if (player == null)
            return;

        if (packet.getWindowId() == 0) { // Player Inventory
            int slot = packet.getSlot();
            net.infcraft.core.item.ItemStack cursor = player.inventory.getCursorItem();
            net.infcraft.core.item.ItemStack clickedStack = null;

            // Map window slot to inventory slot
            // 0-4: Crafting (ignore for now)
            // 5-8: Armor
            // 9-35: Main Inventory
            // 36-44: Hotbar
            if (slot >= 9 && slot < 36) {
                clickedStack = player.inventory.mainInventory[slot - 9 + 9]; // 9-35 -> 9-35? No.
                // InventoryPlayer: 0-8 is hotbar. 9-35 is storage.
                // Window: 9-35 is storage. 36-44 is hotbar.
                // So Window 9-35 maps to Inventory 9-35.
                clickedStack = player.inventory.mainInventory[slot];
            } else if (slot >= 36 && slot < 45) {
                clickedStack = player.inventory.mainInventory[slot - 36]; // 36-44 -> 0-8
            } else if (slot >= 5 && slot < 9) {
                clickedStack = player.inventory.armorInventory[slot - 5];
            }

            // Basic swap logic (simplified)
            if (clickedStack == null && cursor != null) {
                // Place cursor into slot
                setInventorySlot(player, slot, cursor);
                player.inventory.setCursorItem(null);
            } else if (clickedStack != null && cursor == null) {
                // Pickup slot
                player.inventory.setCursorItem(clickedStack);
                setInventorySlot(player, slot, null);
            } else if (clickedStack != null && cursor != null) {
                // Swap
                player.inventory.setCursorItem(clickedStack);
                setInventorySlot(player, slot, cursor);
            }

            // Send update back to client to ensure sync
            // We should send SetSlot for the clicked slot and the cursor (if we had a
            // packet
            // Update the clicked slot
            net.infcraft.shared.network.packets.PacketSetSlot setSlot = new net.infcraft.shared.network.packets.PacketSetSlot(
                    0, slot, getInventorySlot(player, slot));
            try {
                networkManager.sendPacket(setSlot);

                // Update the cursor item (slot -1)
                net.infcraft.shared.network.packets.PacketSetSlot setCursor = new net.infcraft.shared.network.packets.PacketSetSlot(
                        0, -1, player.inventory.getCursorItem());
                networkManager.sendPacket(setCursor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setInventorySlot(net.infcraft.core.entity.EntityPlayer player, int slot,
                                  net.infcraft.core.item.ItemStack stack) {
        if (slot >= 9 && slot < 36) {
            player.inventory.mainInventory[slot] = stack;
        } else if (slot >= 36 && slot < 45) {
            player.inventory.mainInventory[slot - 36] = stack;
        } else if (slot >= 5 && slot < 9) {
            player.inventory.armorInventory[slot - 5] = stack;
        }
    }

    private net.infcraft.core.item.ItemStack getInventorySlot(net.infcraft.core.entity.EntityPlayer player,
                                                              int slot) {
        if (slot >= 9 && slot < 36) {
            return player.inventory.mainInventory[slot];
        } else if (slot >= 36 && slot < 45) {
            return player.inventory.mainInventory[slot - 36];
        } else if (slot >= 5 && slot < 9) {
            return player.inventory.armorInventory[slot - 5];
        }
        return null;
    }

    private void handlePlayerDigging(net.infcraft.shared.network.packets.PacketPlayerDigging packet,
            INetworkManager networkManager) {
        if (server.getWorld() == null)
            return;

        net.infcraft.core.entity.EntityPlayer player = getPlayerFromNetworkManager(networkManager);
        if (player == null)
            return;

        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();

        // Distance check (Reach)
        double distSq = player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5);
        if (distSq > 36.0) { // 6 blocks reach
            System.out.println("Player " + player.getUsername() + " tried to dig too far: " + Math.sqrt(distSq));
            return;
        }

        if (packet.getStatus() == 2) { // Finished digging (Block Broken)
            // Verify block exists
            int blockId = server.getWorld().getBlockId(x, y, z);
            System.out.println("SERVER: Block broken at (" + x + "," + y + "," + z + ") blockId=" + blockId);
            if (blockId > 0) {
                net.infcraft.core.blocks.Block block = net.infcraft.core.blocks.Block.blocksList[blockId];
                // Use playSound(player, ...) to send to everyone EXCEPT the player
                // The client plays the break sound locally immediately
                server.getWorld().playSound(player, block.digSound.digSoundDir(),
                        (block.digSound.soundVolume() + 1.0f) / 2.0f, block.digSound.soundPitch() * 0.8f);
                // Drop items
                if (block != null) {
                    int metadata = server.getWorld().getBlockMetadata(x, y, z);
                    System.out.println("SERVER: Calling dropBlockAsItem for block " + blockId);
                    block.dropBlockAsItem((net.infcraft.server.world.ServerWorld) server.getWorld(), x, y, z,
                            metadata);
                }

                // Break block
                server.getWorld().setBlockWithNotify(x, y, z, 0);

                // Broadcast change to all players (including self, to confirm)
                net.infcraft.shared.network.packets.PacketBlockChange changePacket = new net.infcraft.shared.network.packets.PacketBlockChange(
                        x, y, z, 0);
                broadcastPacketToAll(changePacket);
            }
        } else if (packet.getStatus() == 4) { // Drop item
            player.dropPlayerItemWithRandomChoice(player.inventory.getCurrentItem(), false);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }
    }

    private void handleBlockPlacement(net.infcraft.shared.network.packets.PacketBlockPlacement packet,
            INetworkManager networkManager) {
        if (server.getWorld() == null)
            return;

        net.infcraft.core.entity.EntityPlayer player = getPlayerFromNetworkManager(networkManager);
        if (player == null)
            return;

        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();
        int face = packet.getFace();

        // Distance check
        double distSq = player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5);
        if (distSq > 36.0 && face != 255) { // 255 is special case for using item in air (not supported yet)
            System.out.println("Player " + player.getUsername() + " tried to place too far: " + Math.sqrt(distSq));
            return;
        }

        net.infcraft.core.item.ItemStack heldItem = player.inventory.getCurrentItem();
        System.out.println("SERVER: Block placement - player=" + player.getUsername() +
                " heldItem=" + (heldItem != null ? heldItem.itemID : "null") +
                " at (" + x + "," + y + "," + z + ") face=" + face);
        if (heldItem == null) {
            System.out.println("SERVER: No item in hand, cannot place");
            return; // Nothing to place
        }

        // Calculate target position based on face
        int targetX = x;
        int targetY = y;
        int targetZ = z;

        if (face == 0)
            targetY--;
        if (face == 1)
            targetY++;
        if (face == 2)
            targetZ--;
        if (face == 3)
            targetZ++;
        if (face == 4)
            targetX--;
        if (face == 5)
            targetX++;

        System.out.println("SERVER: Target position: (" + targetX + "," + targetY + "," + targetZ + ")");

        // Check if placement is valid (no collision with player, etc - simplified for
        // now)
        // Also check if block can be placed (id > 0 and < 256)
        if (heldItem.itemID < 256) {
            System.out.println("SERVER: Item is a block (ID=" + heldItem.itemID + "), attempting placement");
            // It's a block
            if (server.getWorld().setBlockAndMetadataWithNotify(targetX, targetY, targetZ, heldItem.itemID,
                    heldItem.itemDamage)) {

                // Play placement sound
                // Broadcast to ALL because client prediction for placement is disabled
                net.infcraft.core.blocks.Block block = net.infcraft.core.blocks.Block.blocksList[heldItem.itemID];
                if (block != null && block.stepSound != null) {
                    server.getWorld().playSoundEffect(targetX + 0.5, targetY + 0.5, targetZ + 0.5,
                            block.digSound.digSoundDir(),
                            (block.digSound.soundVolume() + 1.0f) / 2.0f,
                            block.digSound.soundPitch() * 0.8f);
                }

                System.out.println("SERVER: Block placed successfully!");
                // Consume item
                heldItem.stackSize--;
                if (heldItem.stackSize <= 0) {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                }

                // Send inventory update to client
                int slot = player.inventory.currentItem + 36; // Hotbar slots are 36-44 in window coordinates
                net.infcraft.shared.network.packets.PacketSetSlot inventoryPacket = new net.infcraft.shared.network.packets.PacketSetSlot(
                        0, slot,
                        player.inventory.getCurrentItem());
                sendPacketToPlayer(player.getUsername(), inventoryPacket);

                // Broadcast change
                net.infcraft.shared.network.packets.PacketBlockChange changePacket = new net.infcraft.shared.network.packets.PacketBlockChange(
                        targetX, targetY, targetZ, heldItem.itemID);
                broadcastPacketToAll(changePacket);
            } else {
                System.out.println("SERVER: Block placement failed (setBlockAndMetadataWithNotify returned false)");
            }
        } else {
            System.out.println("SERVER: Item is not a block (ID=" + heldItem.itemID + " >= 256)");
        }
    }

    private void handleHeldItemChange(net.infcraft.shared.network.packets.PacketHeldItemChange packet,
            INetworkManager networkManager) {
        if (server.getWorld() == null)
            return;

        net.infcraft.core.entity.EntityPlayer player = getPlayerFromNetworkManager(networkManager);
        if (player == null)
            return;

        int slot = packet.getSlot();
        if (slot >= 0 && slot < 9) {
            player.inventory.currentItem = slot;
        }
    }

    private net.infcraft.core.entity.EntityPlayer getPlayerFromNetworkManager(INetworkManager networkManager) {
        if (networkManager instanceof LocalNetworkManager) {
            net.infcraft.core.entity.EntityPlayer player = ((LocalNetworkManager) networkManager).getPlayer();
            if (player != null) {
                return player;
            }
        }

        return networkManagerToPlayerMap.get(networkManager);
    }

    private void handleHandshake(net.infcraft.shared.network.packets.PacketHandshake packet,
            INetworkManager networkManager) {
        System.out.println("Received Handshake: " + packet.getUsername() + ", Next State: " + packet.getNextState());

        if (networkManager instanceof RemoteNetworkManager) {
            RemoteNetworkManager remoteManager = (RemoteNetworkManager) networkManager;

            if (packet.getNextState() == 2) { // Login
                // Switch to LOGIN state first
                remoteManager.setConnectionState(net.infcraft.shared.network.ConnectionState.LOGIN);

                // Send LoginSuccess
                net.infcraft.shared.network.packets.PacketLoginSuccess loginSuccess = new net.infcraft.shared.network.packets.PacketLoginSuccess(
                        123, // Entity ID
                        0, 0, 0, // Spawn X, Y, Z (will be overwritten by WorldInit?)
                        packet.getUsername());
                try {
                    remoteManager.sendPacket(loginSuccess);

                    // Now switch to CONFIGURATION
                    remoteManager.setConnectionState(net.infcraft.shared.network.ConnectionState.CONFIGURATION);

                    // Send Registry Data
                    java.util.Map<String, Integer> entries = new java.util.HashMap<>();
                    // Add some dummy entries or real ones if available
                    entries.put("minecraft:air", 0);
                    entries.put("minecraft:stone", 1);
                    entries.put("minecraft:grass", 2);
                    entries.put("minecraft:dirt", 3);

                    net.infcraft.shared.network.packets.PacketRegistryData registryData = new net.infcraft.shared.network.packets.PacketRegistryData(
                            "minecraft:block", entries);
                    remoteManager.sendPacket(registryData);

                    // Send Finish Config
                    remoteManager.sendPacket(new net.infcraft.shared.network.packets.PacketFinishConfig());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleAckFinish(net.infcraft.shared.network.packets.PacketAckFinish packet,
            INetworkManager networkManager) {
        System.out.println("Received AckFinish. Switching to PLAY state.");
        if (networkManager instanceof RemoteNetworkManager) {
            ((RemoteNetworkManager) networkManager)
                    .setConnectionState(net.infcraft.shared.network.ConnectionState.PLAY);

            // Now we can send World Init, Chunk Data, etc.
            // Trigger player join logic here?
            // For now, we assume the client is ready to receive Play packets.
        }
    }

    private void handlePlayerPositionRotation(net.infcraft.shared.network.packets.PacketPlayerPositionRotation packet,
            INetworkManager networkManager) {
        // Update player position in the world
        if (server.getWorld() != null) {
            java.util.List entities = server.getWorld().getLoadedEntityList();
            for (Object obj : entities) {
                if (obj instanceof net.infcraft.core.entity.EntityPlayer) {
                    net.infcraft.core.entity.EntityPlayer player = (net.infcraft.core.entity.EntityPlayer) obj;

                    // Server-Authoritative Movement Validation
                    double dx = packet.getX() - player.posX;
                    double dy = packet.getY() - player.posY;
                    double dz = packet.getZ() - player.posZ;
                    double distanceSquared = dx * dx + dy * dy + dz * dz;

                    // Threshold: 10 blocks per update (100 squared) is generous but catches
                    // teleport hacks
                    // Normal movement is < 1 block per tick. Sprinting is < 2.
                    // We allow a bit more for lag/latency catch-up.
                    if (distanceSquared > 100.0) {
                        System.out.println("WARNING: Player " + player.getUsername() + " moved too fast! ("
                                + Math.sqrt(distanceSquared) + " blocks). Teleporting back to " + player.posX + ","
                                + player.posY + "," + player.posZ);

                        // Send authoritative position back to client to correct them
                        net.infcraft.shared.network.packets.PacketPlayerPositionRotation correctionPacket = new net.infcraft.shared.network.packets.PacketPlayerPositionRotation(
                                (float) player.posX,
                                (float) player.posY,
                                (float) player.posZ,
                                player.rotationYaw,
                                player.rotationPitch,
                                player.onGround);

                        try {
                            networkManager.sendPacket(correctionPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return; // Do not update server state
                    }

                    // Update server entity position
                    int oldChunkX = Mth.floor_double(player.posX) >> 4;
                    int oldChunkZ = Mth.floor_double(player.posZ) >> 4;

                    player.setPositionAndRotation(
                            packet.getX(),
                            packet.getY(),
                            packet.getZ(),
                            packet.getYaw(),
                            packet.getPitch());
                    player.onGround = packet.isOnGround();

                    int newChunkX = Mth.floor_double(player.posX) >> 4;
                    int newChunkZ = Mth.floor_double(player.posZ) >> 4;

                    // If player moved to a new chunk, update loaded chunks dynamically
                    if (oldChunkX != newChunkX || oldChunkZ != newChunkZ) {
                        // Get player username for per-player chunk tracking
                        String username = player.getUsername();
                        if (username != null) {
                            // Use server's configured render distance
                            int renderDistance = server.getConfig().getRenderDistance();
                            updatePlayerChunks(username, newChunkX, newChunkZ, renderDistance);
                        } else {
                            System.err.println("SERVER: Player username is null, cannot update chunks");
                        }
                    }

                    // Break after updating the first player (temporary for singleplayer)
                    break;
                }
            }
        }
    }

    /**
     * Create a network manager for local/integrated server communication
     */
    public INetworkManager createLocalConnection() {
        return new LocalNetworkManager();
    }

    /**
     * Process packets from the local/integrated client
     */
    public void processIntegratedClientPackets() {
        if (localNetworkManager != null) {
            // Process all packets from the local client
            // CRITICAL FIX: Read from outboundPackets (client->server), not inboundPackets
            // (server->client)
            Object packet;
            while ((packet = localNetworkManager.getOutboundPackets().poll()) != null) {
                if (packet instanceof IPacket) {
                    processPacketFromClient((IPacket) packet, null); // null indicates local connection
                }
            }
        }
    }

    /**
     * Send chunk data to a player
     */
    public void sendChunkToPlayer(int chunkX, int chunkZ) {
        if (server.getWorld() != null) {
            // Get the chunk from server world
            net.infcraft.core.world.chunk.Chunk chunk = server.getWorld().getChunkFromChunkCoords(chunkX, chunkZ);
            if (chunk != null) {
                // Create chunk data packet
                PacketChunkData chunkPacket = new PacketChunkData(chunk);

                // Send to integrated client
                sendPacketToIntegratedClient(chunkPacket);
            } else {
                System.out.println("Warning: Chunk (" + chunkX + ", " + chunkZ + ") is null on server!");
            }
        }
    }

    /**
     * Send chunk data to a specific player
     */
    public void sendChunkToPlayer(String username, int chunkX, int chunkZ) {
        if (server.getWorld() != null) {
            // Get the chunk from server world
            net.infcraft.core.world.chunk.Chunk chunk = server.getWorld().getChunkFromChunkCoords(chunkX, chunkZ);
            if (chunk != null) {
                // Create chunk data packet
                PacketChunkData chunkPacket = new PacketChunkData(chunk);

                // Send to specific player
                sendPacketToPlayer(username, chunkPacket);
            } else {
                System.out.println("Warning: Chunk (" + chunkX + ", " + chunkZ + ") is null on server!");
            }
        }
    }

    /**
     * Send initial visible chunks to a player
     */
    public void sendInitialChunksToPlayer(int playerChunkX, int playerChunkZ) {
        // Send chunks in a square around the player (e.g., 3x3 area centered on player)
        int renderDistance = 2; // 2 chunks in each direction from center
        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                sendChunkToPlayer(playerChunkX + dx, playerChunkZ + dz);
            }
        }
    }

    /**
     * Update chunks for a player based on their current position
     * Sends new chunks that are now in range and unloads chunks that are too far
     */
    public void updatePlayerChunks(String username, int playerChunkX, int playerChunkZ, int renderDistance) {
        java.util.Set<Long> loadedChunks = playerLoadedChunks.get(username);
        if (loadedChunks == null) {
            System.err.println("No loaded chunks tracking for player: " + username);
            return;
        }

        // Calculate which chunks should be loaded with priority ordering
        java.util.Set<Long> shouldBeLoaded = new java.util.HashSet<>();
        java.util.List<ChunkLoadRequest> chunksToLoad = new java.util.ArrayList<>();

        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = playerChunkX + dx;
                int chunkZ = playerChunkZ + dz;
                long chunkKey = getChunkKey(chunkX, chunkZ);
                shouldBeLoaded.add(chunkKey);

                // Track chunks that need to be loaded with distance priority
                if (!loadedChunks.contains(chunkKey)) {
                    // Calculate distance from player (for priority sorting)
                    int distanceSquared = dx * dx + dz * dz;
                    chunksToLoad.add(new ChunkLoadRequest(chunkKey, chunkX, chunkZ, distanceSquared));
                }
            }
        }

        // Sort chunks by distance - closest chunks load first (spiral pattern)
        chunksToLoad.sort((a, b) -> Integer.compare(a.distanceSquared, b.distanceSquared));
        // Rate limit: Only load up to 25 chunks per update to prevent lag spikes
        // This is a balance between loading speed and performance
        int chunksLoadedThisUpdate = 0;
        final int MAX_CHUNKS_PER_UPDATE = 25;

        for (ChunkLoadRequest request : chunksToLoad) {
            if (chunksLoadedThisUpdate >= MAX_CHUNKS_PER_UPDATE) {
                break; // Load remaining chunks on next update
            }

            sendChunkToPlayer(username, request.chunkX, request.chunkZ);
            loadedChunks.add(request.chunkKey);
            chunksLoadedThisUpdate++;
        }

        // Unload chunks that are too far away
        java.util.Iterator<Long> iterator = loadedChunks.iterator();
        while (iterator.hasNext()) {
            long chunkKey = iterator.next();
            if (!shouldBeLoaded.contains(chunkKey)) {
                // TODO: Send unload packet to client
                // For now, just remove from tracking
                iterator.remove();
            }
        }

        if (chunksLoadedThisUpdate > 0) {
            System.out.println("Loaded " + chunksLoadedThisUpdate + " chunks for player " + username);
        }
    }

    /**
     * Get a unique key for a chunk coordinate
     */
    private long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    /**
     * Broadcast a packet to all connected clients
     */
    public void broadcastPacketToAll(IPacket packet) {
        // For integrated server mode, notify the client through local connection
        // In multiplayer mode, this would send to all connected remote clients
        System.out.println("Broadcasting packet to all clients: " + packet.getClass().getSimpleName());

        // Send to integrated client
        if (localNetworkManager != null) {
            localNetworkManager.addServerPacket(packet);
        }

        // Send to all remote clients
        for (INetworkManager networkManager : connectedClients.values()) {
            try {
                networkManager.sendPacket(packet);
            } catch (IOException e) {
                System.err.println("Error broadcasting packet: " + e.getMessage());
            }
        }
    }

    /**
     * Broadcast a packet to all connected clients except one
     */
    public void sendPacketToAllExcept(IPacket packet, net.infcraft.core.entity.EntityPlayer playerToExclude) {
        // For integrated server mode, notify the client through local connection
        // ONLY if the excluded player is NOT the local player
        if (localNetworkManager != null) {
            net.infcraft.core.entity.EntityPlayer localPlayer = localNetworkManager.getPlayer();
            if (localPlayer != null && !localPlayer.equals(playerToExclude)) {
                localNetworkManager.addServerPacket(packet);
            }
        }

        // Send to all remote clients
        for (Map.Entry<Socket, INetworkManager> entry : connectedClients.entrySet()) {
            INetworkManager networkManager = entry.getValue();
            net.infcraft.core.entity.EntityPlayer remotePlayer = networkManagerToPlayerMap.get(networkManager);

            if (remotePlayer != null && !remotePlayer.equals(playerToExclude)) {
                try {
                    networkManager.sendPacket(packet);
                } catch (IOException e) {
                    System.err.println("Error broadcasting packet: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Register a player's network manager for per-player packet sending
     */
    public void registerPlayer(net.infcraft.core.entity.EntityPlayer player, INetworkManager networkManager) {
        String username = player.getUsername();
        playerNetworkManagers.put(username, networkManager);
        networkManagerToPlayerMap.put(networkManager, player);
        playerLoadedChunks.put(username, new java.util.HashSet<>());
        playerKnownEntities.put(username, new java.util.HashSet<>());
        System.out.println("Registered player network manager: " + username);
    }

    /**
     * Unregister a player when they disconnect
     */
    public void unregisterPlayer(String username) {
        INetworkManager networkManager = playerNetworkManagers.remove(username);
        if (networkManager != null) {
            networkManagerToPlayerMap.remove(networkManager);
        }
        playerLoadedChunks.remove(username);
        playerKnownEntities.remove(username);
        System.out.println("Unregistered player: " + username);
    }

    /**
     * Add an entity to a player's known entity list
     */
    public void addEntityToPlayer(String username, int entityId) {
        java.util.Set<Integer> knownEntities = playerKnownEntities.get(username);
        if (knownEntities != null) {
            knownEntities.add(entityId);
        }
    }

    /**
     * Remove an entity from a player's known entity list
     */
    public void removeEntityFromPlayer(String username, int entityId) {
        java.util.Set<Integer> knownEntities = playerKnownEntities.get(username);
        if (knownEntities != null) {
            knownEntities.remove(entityId);
        }
    }

    /**
     * Check if a player knows about a specific entity
     */
    public boolean playerKnowsEntity(String username, int entityId) {
        java.util.Set<Integer> knownEntities = playerKnownEntities.get(username);
        return knownEntities != null && knownEntities.contains(entityId);
    }

    /**
     * Clear all known entities for a player
     */
    public void clearPlayerKnownEntities(String username) {
        java.util.Set<Integer> knownEntities = playerKnownEntities.get(username);
        if (knownEntities != null) {
            knownEntities.clear();
        }
    }

    /**
     * Get all registered player usernames
     */
    public java.util.Set<String> getPlayerUsernames() {
        return playerNetworkManagers.keySet();
    }

    /**
     * Get the network manager for a player
     */
    public INetworkManager getPlayerNetworkManager(String username) {
        return playerNetworkManagers.get(username);
    }

    /**
     * Send a packet to a specific player
     */
    public void sendPacketToPlayer(String username, IPacket packet) {
        INetworkManager networkManager = playerNetworkManagers.get(username);
        if (networkManager != null) {
            try {
                if (networkManager instanceof LocalNetworkManager) {
                    ((LocalNetworkManager) networkManager).addServerPacket(packet);
                } else {
                    networkManager.sendPacket(packet);
                }
            } catch (IOException e) {
                System.err.println("Error sending packet to player " + username + ": " + e.getMessage());
            }
        } else {
            System.err.println("No network manager found for player: " + username);
        }
    }

    /**
     * Send a packet to all connected players
     */
    public void sendPacketToAllPlayers(IPacket packet) {
        for (Map.Entry<String, INetworkManager> entry : playerNetworkManagers.entrySet()) {
            sendPacketToPlayer(entry.getKey(), packet);
        }
    }

    /**
     * Send player join/leave notifications
     */
    public void sendPlayerInfo(String playerName, boolean isJoining) {
        try {
            PacketPlayerInfo playerInfoPacket = new PacketPlayerInfo(playerName, 0, isJoining);
            broadcastPacketToAll(playerInfoPacket);
        } catch (Exception e) {
            System.err.println("Error sending player info packet: " + e.getMessage());
        }
    }

    /**
     * Send world initialization data to a player
     */
    public void sendWorldInitToPlayer(String playerName) {
        try {
            // Get world info from the server
            if (server.getWorld() != null) {
                ServerWorld serverWorld = server.getWorld();
                PacketWorldInit worldInitPacket = new PacketWorldInit(
                        serverWorld.x, // spawn X
                        serverWorld.y, // spawn Y
                        serverWorld.z, // spawn Z
                        serverWorld.n, // world seed
                        serverWorld.getWorldTime, // world time
                        serverWorld.p // world name
                );

                // For integrated server, send directly to the local client
                if (localNetworkManager != null) {
                    localNetworkManager.addServerPacket(worldInitPacket);

                    // Also send initial chunks around the spawn point
                    sendInitialChunksToPlayer(serverWorld.x >> 4, serverWorld.z >> 4);
                } else {
                    // For remote connections, broadcast to all players
                    broadcastPacketToAll(worldInitPacket);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending world init packet: " + e.getMessage());
        }
    }

    /**
     * Send a packet to the local/integrated client
     */
    public void sendPacketToIntegratedClient(IPacket packet) {
        if (localNetworkManager != null) {
            localNetworkManager.addServerPacket(packet);
        }
    }

    /**
     * Broadcast entity state updates to all connected clients
     */
    public void broadcastEntityStates(net.infcraft.shared.network.packets.PacketEntityState packet) {
        // Send to integrated client
        if (localNetworkManager != null) {
            localNetworkManager.addServerPacket(packet);
        }

        // Send to all remote clients
        for (INetworkManager networkManager : connectedClients.values()) {
            try {
                networkManager.sendPacket(packet);
            } catch (IOException e) {
                System.err.println("Error broadcasting entity states: " + e.getMessage());
            }
        }
    }

    /**
     * Broadcast an effect event to all connected clients
     */
    public void broadcastEffect(net.infcraft.shared.network.packets.PacketEffect packet) {
        // Send to integrated client
        if (localNetworkManager != null) {
            localNetworkManager.addServerPacket(packet);
        }

        // Send to all remote clients
        for (INetworkManager networkManager : connectedClients.values()) {
            try {
                networkManager.sendPacket(packet);
            } catch (IOException e) {
                System.err.println("Error broadcasting effect: " + e.getMessage());
            }
        }
    }

    /**
     * Broadcast world state update to all connected clients
     */
    public void broadcastWorldState(net.infcraft.shared.network.packets.PacketWorldState packet) {
        // Send to integrated client
        if (localNetworkManager != null) {
            localNetworkManager.addServerPacket(packet);
        }

        // Send to all remote clients
        for (INetworkManager networkManager : connectedClients.values()) {
            try {
                networkManager.sendPacket(packet);
            } catch (IOException e) {
                System.err.println("Error broadcasting world state: " + e.getMessage());
            }
        }
    }

    /**
     * Stop the network system
     */
    public void stop() {
        isRunning = false;

        // Shutdown connection acceptor
        if (connectionAcceptorTask != null) {
            connectionAcceptorTask.cancel(true);
        }

        // Close all client connections
        for (INetworkManager networkManager : connectedClients.values()) {
            networkManager.closeConnection();
        }
        connectedClients.clear();

        // Close server socket
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        networkThreadPool.shutdown();
    }

    /**
     * Get the network thread pool
     */
    public ExecutorService getNetworkThreadPool() {
        return networkThreadPool;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
