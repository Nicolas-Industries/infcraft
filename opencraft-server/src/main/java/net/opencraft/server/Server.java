package net.opencraft.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.server.world.ServerWorld;
import net.opencraft.shared.network.INetworkManager;

/**
 * Main server class that manages the game world and player connections
 */
public class Server {

    public ServerWorld world;
    private List<EntityPlayer> players;
    private List<INetworkManager> networkManagers;
    private Map<INetworkManager, EntityPlayer> playerNetworkMap;
    private boolean isRunning;
    private int maxPlayers;
    private int serverPort;
    private String worldName;
    private File saveDir;

    // Server configuration
    private Thread serverThread;
    public NetworkSystem networkSystem;

    public Server() {
        this.players = new ArrayList<>();
        this.networkManagers = new CopyOnWriteArrayList<>();
        this.playerNetworkMap = new ConcurrentHashMap<>();
        this.maxPlayers = 8; // Default max players
        this.serverPort = 25565; // Default Minecraft server port
        this.isRunning = false;
        this.worldName = "world";
        this.saveDir = new File("saves");
        this.networkSystem = new NetworkSystem(this);
    }
    
    /**
     * Start the server
     */
    public void start() {
        System.out.println("Initializing OpenCraft server...");

        // Initialize the server world
        this.world = new ServerWorld(this.saveDir, this.worldName, this);

        // Initialize the network system
        this.networkSystem.initializeIntegratedServer();

        // Start the network system for remote connections if needed
        this.networkSystem.startRemoteServer(serverPort);

        this.isRunning = true;

        // Start server thread
        serverThread = new Thread(this::runServerLoop, "Server Thread");
        serverThread.start();

        System.out.println("OpenCraft server started on port " + serverPort);
    }
    
    /**
     * Stop the server
     */
    public void stop() {
        System.out.println("Stopping OpenCraft server...");

        isRunning = false;

        // Stop the network system
        networkSystem.stop();

        // Disconnect all players
        for (EntityPlayer player : new ArrayList<>(players)) {
            disconnectPlayer(player);
        }

        // Close all network connections
        for (INetworkManager networkManager : new ArrayList<>(networkManagers)) {
            networkManager.closeConnection();
        }

        // Save world data
        if (world != null) {
            world.saveWorld();
        }

        System.out.println("OpenCraft server stopped");
    }
    
    /**
     * Main server loop
     */
    private void runServerLoop() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000.0 / 20.0; // 20 ticks per second
        int ticks = 0;
        long lastTimer = System.currentTimeMillis();
        
        while (isRunning) {
            long now = System.nanoTime();
            long delta = now - lastTime;
            lastTime = now;
            
            boolean shouldUpdate = false;
            
            while (delta >= nsPerTick) {
                shouldUpdate = true;
                delta -= nsPerTick;
                
                if (isRunning) {
                    try {
                        update();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                ticks++;
            }
            
            if (shouldUpdate) {
                // Process network data
                for (INetworkManager networkManager : new ArrayList<>(networkManagers)) {
                    try {
                        networkManager.processNetworkData();
                    } catch (Exception e) {
                        // Connection may be broken, remove the manager
                        networkManagers.remove(networkManager);
                        e.printStackTrace();
                    }
                }
            }
            
            // Check if a second has passed to print TPS
            if (System.currentTimeMillis() - lastTimer >= 1000) {
                lastTimer += 1000;
                System.out.println("Server TPS: " + ticks);
                ticks = 0;
            }
        }
    }
    
    /**
     * Update the server state
     */
    private void update() {
        // Update the world
        if (world != null) {
            world.update();
        }

        // Update players
        for (EntityPlayer player : new ArrayList<>(players)) {
            player.onUpdate();
        }

        // Process network data from all managers
        for (INetworkManager networkManager : new ArrayList<>(networkManagers)) {
            if (networkManager.isConnectionOpen()) {
                networkManager.processNetworkData();

                // Process any received packets
                Object packet;
                while ((packet = networkManager.getReceivedPacket()) != null) {
                    if (packet instanceof net.opencraft.shared.network.packets.IPacket) {
                        // Handle the received packet
                        processPacketFromClient((net.opencraft.shared.network.packets.IPacket) packet, networkManager);
                    }
                }
            } else {
                // Remove closed connections
                networkManagers.remove(networkManager);
            }
        }
    }

    /**
     * Process a packet from a connected client
     */
    private void processPacketFromClient(net.opencraft.shared.network.packets.IPacket packet, INetworkManager networkManager) {
        System.out.println("Received packet from client: " + packet.getClass().getSimpleName());

        // Handle the packet based on its type
        if (packet instanceof net.opencraft.shared.network.packets.PacketPlayerPosition) {
            handlePlayerPosition((net.opencraft.shared.network.packets.PacketPlayerPosition) packet, networkManager);
        } else if (packet instanceof net.opencraft.shared.network.packets.PacketBlockChange) {
            handleBlockChange((net.opencraft.shared.network.packets.PacketBlockChange) packet, networkManager);
        }
        // Add other packet handlers as needed
    }

    /**
     * Handle player position updates from clients
     * Server validates and processes the position update
     */
    private void handlePlayerPosition(net.opencraft.shared.network.packets.PacketPlayerPosition packet, INetworkManager networkManager) {
        // Get the player associated with this network manager
        EntityPlayer player = playerNetworkMap.get(networkManager);

        // Basic movement validation (simplified)
        // For a complete implementation, you'd track previous positions and validate velocity
        // For now, we'll just validate that the position is within reasonable bounds
        if (packet.getY() < -100 || packet.getY() > 256) {
            System.out.println("Invalid position received, rejecting: (" +
                              packet.getX() + ", " + packet.getY() + ", " + packet.getZ() + ")");
            return; // Reject invalid position
        }

        // Update the player's position in the server world
        if (player != null) {
            player.setPositionAndRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
        }

        // Allow the position update (in a real implementation, validate against max speed)
        System.out.println("Server received player position: (" +
                          packet.getX() + ", " + packet.getY() + ", " + packet.getZ() + ")");

        // Broadcast the position to all other players
        networkSystem.sendPacketToAll(packet);
    }

    /**
     * Handle block change requests from clients
     * Server validates and processes the block change
     */
    private void handleBlockChange(net.opencraft.shared.network.packets.PacketBlockChange packet, INetworkManager networkManager) {
        // Get the player associated with this network manager
        EntityPlayer player = playerNetworkMap.get(networkManager);

        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();
        int blockId = packet.getBlockId();

        // Apply the block change on the server world with player validation
        boolean success = world.setBlock(x, y, z, blockId, player);

        if (success) {
            // Broadcast the block change to all connected clients
            networkSystem.sendPacketToAll(packet);
        } else {
            // In a complete implementation, send error back to client
            System.out.println("Block change rejected: (" + x + ", " + y + ", " + z + ")");
        }
    }
    
    /**
     * Add a player to the server
     */
    public boolean addPlayer(String username, INetworkManager networkManager) {
        if (players.size() >= maxPlayers) {
            return false; // Server is full
        }

        // Create a server-side player
        EntityPlayer player = new EntityPlayer(world);
        // Note: username field may not exist in original EntityPlayer, need to use appropriate mechanism
        // For now, we'll just add to the list without setting username directly

        players.add(player);
        networkManagers.add(networkManager);
        playerNetworkMap.put(networkManager, player);

        // Send world initialization to the new player
        networkSystem.sendWorldInitToClient(networkManager);

        System.out.println("Player " + username + " joined the game. Players: " + players.size());
        return true;
    }
    
    /**
     * Remove a player from the server
     */
    public void disconnectPlayer(EntityPlayer player) {
        players.remove(player);
        // Find and remove the associated network manager
        for (Map.Entry<INetworkManager, EntityPlayer> entry : playerNetworkMap.entrySet()) {
            if (entry.getValue() == player) {
                INetworkManager networkManager = entry.getKey();
                networkManagers.remove(networkManager);
                playerNetworkMap.remove(networkManager);
                break;
            }
        }
        System.out.println("Player disconnected from the game"); // Assuming player has username
    }
    
    /**
     * Get the player associated with a network manager
     */
    public EntityPlayer getPlayerForNetworkManager(INetworkManager networkManager) {
        return playerNetworkMap.get(networkManager);
    }

    /**
     * Get the server world
     */
    public ServerWorld getWorld() {
        return world;
    }
    
    /**
     * Check if the server is running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Get the list of connected players
     */
    public List<EntityPlayer> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * Get the current number of players
     */
    public int getCurrentPlayerCount() {
        return players.size();
    }
    
    /**
     * Get the maximum number of players
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    /**
     * Set the maximum number of players
     */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    /**
     * Get the server port
     */
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * Set the server port
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}