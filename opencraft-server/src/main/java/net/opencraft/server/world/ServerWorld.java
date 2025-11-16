package net.opencraft.server.world;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.tileentity.TileEntity;
import net.opencraft.core.util.CompressedStreamTools;
import net.opencraft.core.blocks.Block;
import net.opencraft.core.entity.Entity;
import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.core.nbt.NBTTagCompound;
import net.opencraft.core.physics.AABB;
import net.opencraft.server.Server;
import net.opencraft.shared.network.packets.PacketBlockChange;
import net.opencraft.shared.network.packets.PacketPlayerPosition;
import net.opencraft.core.world.IBlockAccess;

/**
 * Server-side world implementation that contains the authoritative game state
 */
public class ServerWorld implements IBlockAccess {
    
    // Core world data
    private File saveDir;
    private String levelName;
    
    // World state
    public long worldTime;
    private long seed;
    public int spawnX, spawnY, spawnZ;
    public boolean isNewWorld;
    
    // World properties
    private final int WORLD_SIZE = 1024; // A reasonable world boundary for this implementation
    private int[][][] blocks; // Simplified storage for block IDs
    private byte[][][] metadata; // Block metadata
    
    // Server reference to send packets
    private Server server;
    
    // Entities in the world
    private List<Entity> entities;
    private Map<Integer, Entity> entityMap;
    private int nextEntityId = 1;
    
    public ServerWorld(File saveDir, String levelName, Server server) {
        this.saveDir = saveDir;
        this.levelName = levelName;
        this.server = server;
        
        // Initialize the world arrays
        this.blocks = new int[WORLD_SIZE][256][WORLD_SIZE]; // X, Y, Z
        this.metadata = new byte[WORLD_SIZE][256][WORLD_SIZE];
        this.entities = new ArrayList<>();
        this.entityMap = new ConcurrentHashMap<>();
        
        // Load or create world
        loadOrCreateWorld();
    }
    
    private void loadOrCreateWorld() {
        File worldDir = new File(saveDir, levelName);
        worldDir.mkdirs();
        
        File levelFile = new File(worldDir, "level.dat");
        isNewWorld = !levelFile.exists();
        
        if (!isNewWorld) {
            loadLevelData(levelFile);
        } else {
            createNewWorld();
        }
    }
    
    private void loadLevelData(File levelFile) {
        try {
            NBTTagCompound compoundTag = CompressedStreamTools.loadGzippedCompoundFromOutputStream(
                new java.io.FileInputStream(levelFile)).getCompoundTag("Data");
            
            this.seed = compoundTag.getLong("RandomSeed");
            this.spawnX = compoundTag.getInteger("SpawnX");
            this.spawnY = compoundTag.getInteger("SpawnY");
            this.spawnZ = compoundTag.getInteger("SpawnZ");
            this.worldTime = compoundTag.getLong("Time");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            createNewWorld(); // Fallback to new world if loading fails
        }
    }
    
    private void createNewWorld() {
        this.seed = System.currentTimeMillis();
        this.spawnX = 0;
        this.spawnY = 64;
        this.spawnZ = 0;
        this.worldTime = 0L;
        
        // Generate a basic world - ground at y=64
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                // Ground level
                blocks[x][64][z] = Block.grass.blockID;
                // Dirt below
                blocks[x][63][z] = Block.dirt.blockID;
                blocks[x][62][z] = Block.dirt.blockID;
                blocks[x][61][z] = Block.dirt.blockID;
                // Stone below that
                for (int y = 0; y < 61; y++) {
                    blocks[x][y][z] = Block.stone.blockID;
                }
            }
        }
    }
    
    public void update() {
        // Update world time
        worldTime++;
        
        // Update entities
        for (Entity entity : new ArrayList<>(entities)) {
            if (!entity.isDead) {
                entity.onUpdate();
            }
        }
    }
    
    public void saveWorld() {
        File worldDir = new File(saveDir, levelName);
        worldDir.mkdirs();
        
        File levelFile = new File(worldDir, "level.dat");
        
        try {
            NBTTagCompound levelData = new NBTTagCompound();
            NBTTagCompound compoundTag = new NBTTagCompound();
            
            compoundTag.setLong("RandomSeed", seed);
            compoundTag.setInteger("SpawnX", spawnX);
            compoundTag.setInteger("SpawnY", spawnY);
            compoundTag.setInteger("SpawnZ", spawnZ);
            compoundTag.setLong("Time", worldTime);
            
            levelData.setTag("Data", compoundTag);
            
            CompressedStreamTools.writeGzippedCompoundToOutputStream(levelData, new java.io.FileOutputStream(levelFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Server-side block management with validation
    public boolean setBlock(int x, int y, int z, int blockId, EntityPlayer player) {
        // Validate that the player is allowed to place/break this block
        // This is a simplified validation - in a real implementation you'd check:
        // - Distance from player (to prevent hacking)
        // - Player permissions/gamemode
        // - Block type restrictions

        if (x < 0 || y < 0 || z < 0 || x >= WORLD_SIZE || y >= 256 || z >= WORLD_SIZE) {
            return false; // Don't allow setting blocks outside world
        }

        // Check if player is within reasonable distance (simplified check)
        if (player != null) {
            double distance = Math.sqrt(Math.pow(x - player.posX, 2) +
                                      Math.pow(y - player.posY, 2) +
                                      Math.pow(z - player.posZ, 2));
            if (distance > 8.0) { // Max interaction distance
                return false; // Too far away
            }
        }

        blocks[x][y][z] = blockId;

        // Send block change packet to all clients
        if (server != null) {
            server.networkSystem.sendPacketToAll(new PacketBlockChange(x, y, z, blockId, 0));
        }

        return true;
    }

    public boolean setBlock(int x, int y, int z, int blockId) {
        return setBlock(x, y, z, blockId, null); // For server-initiated changes
    }
    
    // IBlockAccess interface implementations
    @Override
    public int getBlockId(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= WORLD_SIZE || y >= 256 || z >= WORLD_SIZE) {
            return 0; // Return air for out of bounds
        }
        return blocks[x][y][z];
    }
    
    @Override
    public int getBlockMetadata(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= WORLD_SIZE || y >= 256 || z >= WORLD_SIZE) {
            return 0; // Return 0 for out of bounds
        }
        return metadata[x][y][z];
    }

    @Override
    public Material getBlockMaterial(int nya1, int nya2, int nya3) {
        return null;
    }

    @Override
    public TileEntity getBlockTileEntity(int x, int y, int z) {
        // For this implementation, we'll return null as we don't have tile entities
        return null;
    }
    
    @Override
    public float getLightBrightness(int x, int y, int z) {
        // Simple light calculation - brighter at higher elevations, full brightness during day
        return 1.0f;
    }
    
    @Override
    public boolean isBlockNormalCube(int x, int y, int z) {
        final Block block = Block.blocksList[this.getBlockId(x, y, z)];
        return block != null && block.isOpaqueCube();
    }
    
    // Entity management
    public int addEntity(Entity entity) {
        int entityId = nextEntityId++;
        //entity.setEntityId(entityId); TODO: implement entity IDs
        entities.add(entity);
        entityMap.put(entityId, entity);
        
        // If it's a player, notify other players
        if (entity instanceof EntityPlayer) {
            // Send player info to all other players
            // TODO: implement usernames
            //server.networkSystem.sendPacketToAll(new net.opencraft.shared.network.packets.PacketPlayerInfo(entityId,
            //    ((EntityPlayer) entity).username));
        }
        
        return entityId;
    }
    
    public void removeEntity(Entity entity) {
        entities.remove(entity);
        //ntityMap.remove(entity.getEntityId()); TODO: requires entity IDs
    }
    
    public Entity getEntityById(int id) {
        return entityMap.get(id);
    }
    
    // Player management
    public void updatePlayerPosition(EntityPlayer player, double x, double y, double z, float yaw, float pitch) {
        player.setPositionAndRotation(x, y, z, yaw, pitch);

        // Send position update to other players todo: REQUIRES ENTITY IDS
        //server.networkSystem.sendPacketToAll(new PacketPlayerPosition(player.getEntityId(), x, y, z, yaw, pitch));
    }

    /**
     * Validate player movement to prevent cheating
     */
    public boolean isValidMovement(EntityPlayer player, double newX, double newY, double newZ,
                                  double oldX, double oldY, double oldZ, long deltaTimeMs) {
        // Calculate distance moved
        double dx = newX - oldX;
        double dy = newY - oldY;
        double dz = newZ - oldZ;
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);

        // Calculate max allowed distance based on time delta and max speed
        // Assuming max speed of about 10 blocks per second for a fast player
        double maxSpeed = 10.0; // blocks per second
        double maxDistance = maxSpeed * (deltaTimeMs / 1000.0);

        // Allow some tolerance for network lag and precision issues
        maxDistance *= 1.1; // 10% tolerance

        return distance <= maxDistance;
    }
    
    /**
     * Create a world initialization packet for new clients
     */
    public net.opencraft.shared.network.packets.PacketWorldInit createWorldInitPacket() {
        // For simplicity, we'll send only a portion of the world
        // In a real implementation, we would implement chunk-based loading
        int sendSizeX = Math.min(WORLD_SIZE, 64);  // Send smaller portion initially
        int sendSizeY = 256;
        int sendSizeZ = Math.min(WORLD_SIZE, 64);

        int[][][] partialBlocks = new int[sendSizeX][sendSizeY][sendSizeZ];

        // Copy only the center portion of the world to send initially
        int offsetX = (WORLD_SIZE - sendSizeX) / 2;
        int offsetZ = (WORLD_SIZE - sendSizeZ) / 2;

        for (int x = 0; x < sendSizeX; x++) {
            for (int y = 0; y < sendSizeY; y++) {
                for (int z = 0; z < sendSizeZ; z++) {
                    partialBlocks[x][y][z] = blocks[offsetX + x][y][offsetZ + z];
                }
            }
        }

        return new net.opencraft.shared.network.packets.PacketWorldInit(
            sendSizeX, sendSizeY, sendSizeZ,
            partialBlocks, worldTime, spawnX, spawnY, spawnZ
        );
    }

    // Getters
    public File getSaveDir() {
        return saveDir;
    }
    
    public String getLevelName() {
        return levelName;
    }
    
    public long getSeed() {
        return seed;
    }
    
    public long getWorldTime() {
        return worldTime;
    }
    
    public int getSpawnX() {
        return spawnX;
    }
    
    public int getSpawnY() {
        return spawnY;
    }
    
    public int getSpawnZ() {
        return spawnZ;
    }
    
    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
    }
}