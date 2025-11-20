package net.opencraft.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.opencraft.server.world.ServerWorld;
import net.opencraft.server.Server;
import net.opencraft.shared.network.INetworkManager;
import net.opencraft.shared.network.LocalNetworkManager;
import net.opencraft.shared.network.PacketManager;
import net.opencraft.shared.network.RemoteNetworkManager;
import net.opencraft.shared.network.packets.*;

import net.opencraft.core.util.Mth;

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
    private Map<String, java.util.Set<Long>> playerLoadedChunks; // username -> set of loaded chunk keys

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
        this.playerLoadedChunks = new ConcurrentHashMap<>();
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

        if (packet instanceof net.opencraft.shared.network.packets.PacketHandshake) {
            handleHandshake((net.opencraft.shared.network.packets.PacketHandshake) packet, networkManager);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketAckFinish) {
            handleAckFinish((net.opencraft.shared.network.packets.PacketAckFinish) packet, networkManager);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketKeepAlive) {
            // Respond to KeepAlive? Or just ignore for now.
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketChatMessage) {
            // Handle chat
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketPlayerPosition) {
            // Handle old-style player position packet

            // reconstruct
            handlePlayerPositionRotation(
                    new net.opencraft.shared.network.packets.PacketPlayerPositionRotation(
                            (float) ((PacketPlayerPosition) packet).getX(),
                            (float) ((PacketPlayerPosition) packet).getY(),
                            (float) ((PacketPlayerPosition) packet).getZ(),
                            ((net.opencraft.shared.network.packets.PacketPlayerPosition) packet).getYaw(),
                            ((net.opencraft.shared.network.packets.PacketPlayerPosition) packet).getPitch(),
                            ((net.opencraft.shared.network.packets.PacketPlayerPosition) packet).isOnGround()
                    ), networkManager); // Pass the networkManager
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketPlayerPositionRotation) {
            // Handle new-style player position packet
            handlePlayerPositionRotation(
                    (net.opencraft.shared.network.packets.PacketPlayerPositionRotation) packet,
                    networkManager);
        } else {
            // Existing handling
            switch (packet.getPacketId()) {
                case 0x22: // Player Digging
                    if (packet instanceof net.opencraft.shared.network.packets.PacketPlayerDigging) {
                        // Handle digging
                    }
                    break;
                case 0x23: // Block Placement
                    if (packet instanceof net.opencraft.shared.network.packets.PacketBlockPlacement) {
                        // Handle placement
                    }
                    break;
                default:
                    // System.out.println("Unhandled packet ID: 0x" +
                    // Integer.toHexString(packet.getPacketId()));
                    break;
            }
        }
    }

    private void handleHandshake(net.opencraft.shared.network.packets.PacketHandshake packet,
            INetworkManager networkManager) {
        System.out.println("Received Handshake: " + packet.getUsername() + ", Next State: " + packet.getNextState());

        if (networkManager instanceof RemoteNetworkManager) {
            RemoteNetworkManager remoteManager = (RemoteNetworkManager) networkManager;

            if (packet.getNextState() == 2) { // Login
                // Switch to LOGIN state first
                remoteManager.setConnectionState(net.opencraft.shared.network.ConnectionState.LOGIN);

                // Send LoginSuccess
                net.opencraft.shared.network.packets.PacketLoginSuccess loginSuccess = new net.opencraft.shared.network.packets.PacketLoginSuccess(
                        123, // Entity ID
                        0, 0, 0, // Spawn X, Y, Z (will be overwritten by WorldInit?)
                        packet.getUsername());
                try {
                    remoteManager.sendPacket(loginSuccess);

                    // Now switch to CONFIGURATION
                    remoteManager.setConnectionState(net.opencraft.shared.network.ConnectionState.CONFIGURATION);

                    // Send Registry Data
                    java.util.Map<String, Integer> entries = new java.util.HashMap<>();
                    // Add some dummy entries or real ones if available
                    entries.put("minecraft:air", 0);
                    entries.put("minecraft:stone", 1);
                    entries.put("minecraft:grass", 2);
                    entries.put("minecraft:dirt", 3);

                    net.opencraft.shared.network.packets.PacketRegistryData registryData = new net.opencraft.shared.network.packets.PacketRegistryData(
                            "minecraft:block", entries);
                    remoteManager.sendPacket(registryData);

                    // Send Finish Config
                    remoteManager.sendPacket(new net.opencraft.shared.network.packets.PacketFinishConfig());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleAckFinish(net.opencraft.shared.network.packets.PacketAckFinish packet,
            INetworkManager networkManager) {
        System.out.println("Received AckFinish. Switching to PLAY state.");
        if (networkManager instanceof RemoteNetworkManager) {
            ((RemoteNetworkManager) networkManager)
                    .setConnectionState(net.opencraft.shared.network.ConnectionState.PLAY);

            // Now we can send World Init, Chunk Data, etc.
            // Trigger player join logic here?
            // For now, we assume the client is ready to receive Play packets.
        }
    }

    private void handlePlayerPositionRotation(net.opencraft.shared.network.packets.PacketPlayerPositionRotation packet,
            INetworkManager networkManager) {
        // Update player position in the world
        if (server.getWorld() != null) {
            java.util.List entities = server.getWorld().getLoadedEntityList();
            for (Object obj : entities) {
                if (obj instanceof net.opencraft.core.entity.EntityPlayer) {
                    net.opencraft.core.entity.EntityPlayer player = (net.opencraft.core.entity.EntityPlayer) obj;

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
            net.opencraft.core.world.chunk.Chunk chunk = server.getWorld().getChunkFromChunkCoords(chunkX, chunkZ);
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
            net.opencraft.core.world.chunk.Chunk chunk = server.getWorld().getChunkFromChunkCoords(chunkX, chunkZ);
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
     * Register a player's network manager for per-player packet sending
     */
    public void registerPlayer(String username, INetworkManager networkManager) {
        playerNetworkManagers.put(username, networkManager);
        playerLoadedChunks.put(username, new java.util.HashSet<>());
        System.out.println("Registered player network manager: " + username);
    }

    /**
     * Unregister a player when they disconnect
     */
    public void unregisterPlayer(String username) {
        playerNetworkManagers.remove(username);
        playerLoadedChunks.remove(username);
        System.out.println("Unregistered player: " + username);
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
    public void broadcastEntityStates(net.opencraft.shared.network.packets.PacketEntityState packet) {
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
    public void broadcastEffect(net.opencraft.shared.network.packets.PacketEffect packet) {
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
    public void broadcastWorldState(net.opencraft.shared.network.packets.PacketWorldState packet) {
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
    }}
























