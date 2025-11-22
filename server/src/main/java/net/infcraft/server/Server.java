package net.infcraft.server;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.infcraft.core.NetworkSystem;
import net.infcraft.core.entity.EntityPlayer;
import net.infcraft.core.nbt.NBTTagCompound;
import net.infcraft.core.util.Mth;
import net.infcraft.server.world.ServerWorld;
import net.infcraft.shared.network.packets.PacketWorldInit;
import net.infcraft.shared.network.packets.PacketPlayerPosition;

/**
 * Server class that manages the game world and handles network connections
 */
public class Server {

    private ServerWorld serverWorld;
    private List<EntityPlayer> players;
    private boolean isRunning;
    private String worldName;
    private File saveDir;

    // Server configuration
    private ServerConfig config;
    private boolean onlineMode;

    // Networking
    private NetworkSystem networkSystem;
    private ExecutorService serverExecutor;

    // Player management
    private Map<String, EntityPlayer> playerList;

    // Tick counter for periodic updates
    private int tickCounter = 0;

    /**
     * Constructor for dedicated server (loads config from server.properties)
     */
    public Server(String worldName, File saveDir) {
        this(worldName, saveDir, null);
    }

    /**
     * Constructor for integrated server (uses client config)
     */
    public Server(String worldName, File saveDir, ServerConfig config) {
        this.worldName = worldName;
        this.saveDir = saveDir;
        this.players = new ArrayList<>();
        this.playerList = new ConcurrentHashMap<>();
        this.onlineMode = true; // Default to online mode
        this.isRunning = false;
        this.serverExecutor = Executors.newFixedThreadPool(4);

        // Load or use provided config
        if (config == null) {
            // Check if this is a dedicated server or integrated server
            // Dedicated servers have saveDir outside of the saves folder
            // Integrated servers have saveDir inside saves/WorldName
            boolean isDedicatedServer = !saveDir.getAbsolutePath().contains("saves");

            if (isDedicatedServer) {
                // Dedicated server - load from server.properties in server root
                File propertiesFile = new File("server.properties");
                this.config = ServerConfig.loadFromFile(propertiesFile);
            } else {
                // Integrated server without config - use defaults
                this.config = new ServerConfig();
                System.out.println("Integrated server using default configuration");
            }
        } else {
            // Integrated server - use provided config
            this.config = config;
            System.out.println("Integrated server using client render distance: " + config.getRenderDistance());
        }
    }

    /**
     * Start the server
     */
    public void start() {
        isRunning = true;

        // Initialize the world
        initializeWorld();

        // Initialize network system
        networkSystem = new NetworkSystem(this);
        networkSystem.initializeIntegratedServer();

        System.out.println("InfCraft server started on port " + config.getServerPort());
        System.out.println("Server name: " + config.getServerName());
        System.out.println("Max players: " + config.getMaxPlayers());
    }

    /**
     * Stop the server
     */
    public void stop() {
        isRunning = false;

        // Disconnect all players
        for (EntityPlayer player : new ArrayList<>(players)) {
            disconnectPlayer(player);
        }

        // Shutdown network system
        if (networkSystem != null) {
            networkSystem.stop();
        }

        // Shutdown executor
        if (serverExecutor != null) {
            serverExecutor.shutdown();
        }

        // Save world
        if (serverWorld != null) {
            EntityPlayer playerToSave = null;
            if (!players.isEmpty()) {
                playerToSave = players.get(0);
            }
            serverWorld.saveWorld(true, null, playerToSave);
        }

        System.out.println("InfCraft server stopped");
    }

    /**
     * Initialize the server world
     */
    private void initializeWorld() {
        // Create world instance for the server
        if (saveDir != null) {
            this.serverWorld = new ServerWorld(saveDir, worldName, this);
        } else {
            this.serverWorld = new ServerWorld(new File("."), "world", new Random().nextLong(), this);
        }
        // Always register ServerWorldManager to handle entity spawning, item drops,
        // etc.
        this.serverWorld.addWorldAccess(new net.infcraft.server.world.ServerWorldManager(this, this.serverWorld));
        System.out.println("World initialized: " + worldName);
    }

    /**
     * Add a player to the server
     */
    public boolean addPlayer(String username, String uuid) {
        if (players.size() >= config.getMaxPlayers()) {
            return false; // Server is full
        }

        // Create player instance
        EntityPlayer player = new EntityPlayer(serverWorld);
        player.setUsername(username);

        // Generate offline UUID based on username
        if (uuid == null) {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)).toString();
        }
        player.setUuid(uuid);

        // Set player position to world spawn point
        if (serverWorld != null) {
            NBTTagCompound playerData = serverWorld.readPlayerData(player);
            if (playerData != null) {
                // Player data loaded successfully
            } else {
                double spawnX = serverWorld.x + 0.5;
                double spawnY = serverWorld.y;
                double spawnZ = serverWorld.z + 0.5;

                // Safety check: Ensure we don't spawn inside a block
                int blockX = Mth.floor_double(spawnX);
                int blockZ = Mth.floor_double(spawnZ);
                int surfaceY = 63;

                // Find actual surface height
                // Check up to 128 blocks high
                for (int y = 63; y < 128; y++) {
                    if (serverWorld.getBlockId(blockX, y + 1, blockZ) == 0) {
                        surfaceY = y;
                        break;
                    }
                }

                // If spawn Y is below surface, move it up
                if (spawnY <= surfaceY) {
                    System.out.println("Adjusting player spawn from Y=" + spawnY + " to surface Y=" + (surfaceY + 3));
                    spawnY = surfaceY + 3;
                }

                player.setPositionAndRotation(spawnX, spawnY, spawnZ, 0, 0);
            }
        }

        players.add(player);
        if (uuid != null) {
            playerList.put(uuid, player);
        }

        // Add player to world
        if (serverWorld != null) {
            // Load chunks at the player's actual position (whether from NBT or spawn)
            int playerChunkX = Mth.floor_double(player.posX) >> 4;
            int playerChunkZ = Mth.floor_double(player.posZ) >> 4;

            // Ensure the chunk at player's position is loaded before adding them to the
            // world
            serverWorld.getChunkFromChunkCoords(playerChunkX, playerChunkZ);
            serverWorld.entityJoinedWorld(player);
        }

        // Send player join notification
        if (networkSystem != null) {
            // Register player for per-player packet sending
            networkSystem.registerPlayer(player, networkSystem.getLocalNetworkManager());

            networkSystem.sendPlayerInfo(username, true);

            // Send world initialization to the new player ONLY
            networkSystem.sendPacketToPlayer(username, new PacketWorldInit(
                    serverWorld.x, serverWorld.y, serverWorld.z,
                    serverWorld.n, serverWorld.getWorldTime, serverWorld.p));

            // Send initial chunks to this player only - use player's actual position
            networkSystem.sendInitialChunksToPlayer(Mth.floor_double(player.posX) >> 4,
                    Mth.floor_double(player.posZ) >> 4);

            // CRITICAL: Send player spawn packet to force client to create the player at
            // the correct location
            // This matches modern Minecraft behavior where the server explicitly spawns the
            // player.
            System.out.println("SERVER: Sending initial spawn packet: (" +
                    player.posX + ", " + player.posY + ", " + player.posZ + ")");

            // Use entity ID 0 for the player for now, or generate one
            int entityId = player.hashCode(); // Temporary ID generation
            net.infcraft.shared.network.packets.PacketPlayerSpawn spawnPacket = new net.infcraft.shared.network.packets.PacketPlayerSpawn(
                    entityId,
                    player.posX, player.posY, player.posZ,
                    player.rotationYaw, player.rotationPitch,
                    username);
            networkSystem.sendPacketToPlayer(username, spawnPacket);

            // Also send position packet to be sure
            PacketPlayerPosition posPacket = new PacketPlayerPosition(
                    player.posX, player.posY, player.posZ,
                    player.rotationYaw, player.rotationPitch, player.onGround);
            networkSystem.sendPacketToPlayer(username, posPacket);

            // Send initial inventory
            net.infcraft.core.item.ItemStack[] windowItems = new net.infcraft.core.item.ItemStack[45];
            // 0-4: Crafting (empty)
            // 5-8: Armor
            for (int i = 0; i < 4; i++) {
                windowItems[5 + i] = player.inventory.armorInventory[i];
            }
            // 9-35: Main Inventory (storage)
            for (int i = 9; i < 36; i++) {
                windowItems[i] = player.inventory.mainInventory[i];
            }
            // 36-44: Hotbar
            for (int i = 0; i < 9; i++) {
                windowItems[36 + i] = player.inventory.mainInventory[i];
            }

            net.infcraft.shared.network.packets.PacketWindowItems inventoryPacket = new net.infcraft.shared.network.packets.PacketWindowItems(
                    0, windowItems);
            networkSystem.sendPacketToPlayer(username, inventoryPacket);
        }

        System.out.println("Player " + username + " joined the game");
        return true;
    }

    /**
     * Remove a player from the server
     */
    public void disconnectPlayer(EntityPlayer player) {
        players.remove(player);
        if (player.getUuid() != null) {
            playerList.remove(player.getUuid());
        }

        // Remove from world if world exists
        if (serverWorld != null) {
            serverWorld.writePlayerData(player);
            serverWorld.setEntityDead(player);
        }

        System.out.println("Player " + player.getUsername() + " left the game");
    }

    /**
     * Get player by UUID
     */
    public EntityPlayer getPlayer(String uuid) {
        return playerList.get(uuid);
    }

    /**
     * Get the server world
     */
    public ServerWorld getWorld() {
        return serverWorld;
    }

    /**
     * Get network system
     */
    public NetworkSystem getNetworkSystem() {
        return networkSystem;
    }

    /**
     * Process server tick - handles network communication and world updates
     */
    public void tick() {
        tickCounter++;

        // Process packets from integrated client if available
        if (networkSystem != null) {
            networkSystem.processIntegratedClientPackets();
        }

        // Tick the world
        if (serverWorld != null) {
            serverWorld.tick();
        }

        // Broadcast player positions every 2 ticks for smooth client-side prediction
        // This provides 10 updates per second (20 ticks/sec ÷ 2)
        // DISABLED: This causes a feedback loop where the server constantly resets the
        // client's position
        // The server should only broadcast when it receives an update from the client
        // if (tickCounter % 2 == 0 && networkSystem != null && serverWorld != null) {
        // broadcastPlayerPositions();
        // }

        // TODO: Re-enable broadcasting after fixing performance issues
        // Broadcast world state every 20 ticks (once per second)
        // if (tickCounter % 20 == 0 && networkSystem != null && serverWorld != null) {
        // broadcastWorldState();
        // }

        // Broadcast entity states every tick for smooth movement
        // if (networkSystem != null && serverWorld != null) {
        // broadcastEntityStates();
        // }
    }

    /**
     * Broadcast world state to all connected clients
     */
    private void broadcastWorldState() {
        try {
            net.infcraft.shared.network.packets.PacketWorldState worldStatePacket = new net.infcraft.shared.network.packets.PacketWorldState(
                    serverWorld.getWorldTime,
                    false, // isRaining - placeholder, would need to be added to ServerWorld
                    0.0f, // rainStrength - placeholder
                    false, // isThundering - placeholder
                    0.0f // thunderStrength - placeholder
            );
            networkSystem.broadcastWorldState(worldStatePacket);
        } catch (Exception e) {
            System.err.println("Error broadcasting world state: " + e.getMessage());
        }
    }

    /**
     * Broadcast entity states to all connected clients
     */
    private void broadcastEntityStates() {
        // TODO: Implement entity state broadcasting
        // This requires proper access to ServerWorld's entity list and entity fields
        // For now, this is a placeholder that can be filled in when entity
        // synchronization is needed

        /*
         * try {
         * java.util.List<net.infcraft.shared.network.packets.PacketEntityState.
         * EntityData> entityDataList =
         * ```
         * new java.util.ArrayList<>();
         * 
         * // Would collect all entities from the world here
         * // and create EntityData for each one
         * 
         * if (!entityDataList.isEmpty()) {
         * net.infcraft.shared.network.packets.PacketEntityState entityStatePacket =
         * new net.infcraft.shared.network.packets.PacketEntityState(entityDataList);
         * networkSystem.broadcastEntityStates(entityStatePacket);
         * }
         * } catch (Exception e) {
         * System.err.println("Error broadcasting entity states: " + e.getMessage());
         * }
         */
    }

    /**
     * Broadcast player positions to all connected clients
     * This is used for hybrid client-side prediction with server reconciliation
     */
    private void broadcastPlayerPositions() {
        if (networkSystem == null || serverWorld == null) {
            return;
        }

        try {
            // Get all entities from the world
            java.util.List entityList = serverWorld.getLoadedEntityList();

            // Broadcast position for each player entity
            for (Object obj : entityList) {
                if (obj instanceof net.infcraft.core.entity.EntityPlayer) {
                    net.infcraft.core.entity.EntityPlayer player = (net.infcraft.core.entity.EntityPlayer) obj;

                    // Create position packet with server-authoritative data
                    net.infcraft.shared.network.packets.PacketPlayerPosition packet = new net.infcraft.shared.network.packets.PacketPlayerPosition(
                            player.posX,
                            player.posY,
                            player.posZ,
                            player.rotationYaw,
                            player.rotationPitch,
                            player.onGround);

                    // Broadcast to all clients (in singleplayer, just the one client)
                    networkSystem.broadcastPacketToAll(packet);
                }
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting player positions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if the server is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get current number of players
     */
    public int getCurrentPlayerCount() {
        return players.size();
    }

    /**
     * Get max number of players
     */
    public int getMaxPlayers() {
        return config.getMaxPlayers();
    }

    /**
     * Get server name
     */
    public String getServerName() {
        return config.getServerName();
    }

    /**
     * Set server name
     */
    public void setServerName(String serverName) {
        config.setServerName(serverName);
    }

    /**
     * Set max players
     */
    public void setMaxPlayers(int maxPlayers) {
        config.setMaxPlayers(maxPlayers);
    }

    /**
     * Set server port
     */
    public void setServerPort(int serverPort) {
        config.setServerPort(serverPort);
    }

    /**
     * Get server port
     */
    public int getServerPort() {
        return config.getServerPort();
    }

    /**
     * Get server configuration
     */
    public ServerConfig getConfig() {
        return config;
    }

    /**
     * Set online mode
     */
    public void setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode;
    }

    /**
     * Get online mode
     */
    public boolean isOnlineMode() {
        return onlineMode;
    }
}