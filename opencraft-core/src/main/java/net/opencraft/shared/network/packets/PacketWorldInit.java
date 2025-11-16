package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

/**
 * Packet to initialize the client with the world state
 */
public class PacketWorldInit implements IPacket {
    
    private int worldSizeX;
    private int worldSizeY;
    private int worldSizeZ;
    private int[][][] blocks;
    private long worldTime;
    private int spawnX, spawnY, spawnZ;
    
    // Default constructor for deserialization
    public PacketWorldInit() {}
    
    public PacketWorldInit(int worldSizeX, int worldSizeY, int worldSizeZ, int[][][] blocks, 
                          long worldTime, int spawnX, int spawnY, int spawnZ) {
        this.worldSizeX = worldSizeX;
        this.worldSizeY = worldSizeY;
        this.worldSizeZ = worldSizeZ;
        this.blocks = blocks;
        this.worldTime = worldTime;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) throws java.io.IOException {
        worldSizeX = buffer.readInt();
        worldSizeY = buffer.readInt();
        worldSizeZ = buffer.readInt();
        worldTime = buffer.readLong();
        spawnX = buffer.readInt();
        spawnY = buffer.readInt();
        spawnZ = buffer.readInt();

        // Read blocks array
        blocks = new int[worldSizeX][worldSizeY][worldSizeZ];
        for (int x = 0; x < worldSizeX; x++) {
            for (int y = 0; y < worldSizeY; y++) {
                for (int z = 0; z < worldSizeZ; z++) {
                    blocks[x][y][z] = buffer.readInt();
                }
            }
        }
    }

    @Override
    public void writePacketData(PacketBuffer buffer) throws java.io.IOException {
        buffer.writeInt(worldSizeX);
        buffer.writeInt(worldSizeY);
        buffer.writeInt(worldSizeZ);
        buffer.writeLong(worldTime);
        buffer.writeInt(spawnX);
        buffer.writeInt(spawnY);
        buffer.writeInt(spawnZ);

        // Write blocks array
        for (int x = 0; x < worldSizeX; x++) {
            for (int y = 0; y < worldSizeY; y++) {
                for (int z = 0; z < worldSizeZ; z++) {
                    buffer.writeInt(blocks[x][y][z]);
                }
            }
        }
    }

    @Override
    public int getPacketId() {
        return 0x0A; // World init packet ID
    }
    
    // Getters
    public int getWorldSizeX() { return worldSizeX; }
    public int getWorldSizeY() { return worldSizeY; }
    public int getWorldSizeZ() { return worldSizeZ; }
    public int[][][] getBlocks() { return blocks; }
    public long getWorldTime() { return worldTime; }
    public int getSpawnX() { return spawnX; }
    public int getSpawnY() { return spawnY; }
    public int getSpawnZ() { return spawnZ; }

    @Override
    public boolean isServerToClient() {
        return true; // This packet goes from server to client
    }

    @Override
    public boolean isClientToServer() {
        return false; // This packet does not go from client to server
    }
}