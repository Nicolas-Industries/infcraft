package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

/**
 * Packet to initialize the client world from server
 */
public class PacketWorldInit implements IPacket {
    private int spawnX, spawnY, spawnZ;
    private long worldSeed;
    private long worldTime;
    private String worldName;

    public PacketWorldInit() {
        // Default constructor for deserialization
    }

    public PacketWorldInit(int spawnX, int spawnY, int spawnZ, long worldSeed, long worldTime, String worldName) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.worldSeed = worldSeed;
        this.worldTime = worldTime;
        this.worldName = worldName;
    }

    @Override
    public int getPacketId() {
        return 0x01; // World initialization packet ID
    }

    @Override
    public boolean isServerToClient() {
        return true; // Server sends this to client
    }

    @Override
    public boolean isClientToServer() {
        return false;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) throws java.io.IOException {
        spawnX = buffer.readInt();
        spawnY = buffer.readInt();
        spawnZ = buffer.readInt();
        worldSeed = buffer.readLong();
        worldTime = buffer.readLong();
        worldName = buffer.readString();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) throws java.io.IOException {
        buffer.writeInt(spawnX);
        buffer.writeInt(spawnY);
        buffer.writeInt(spawnZ);
        buffer.writeLong(worldSeed);
        buffer.writeLong(worldTime);
        buffer.writeString(worldName);
    }

    // Getters
    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public int getSpawnZ() {
        return spawnZ;
    }

    public long getWorldSeed() {
        return worldSeed;
    }

    public long getWorldTime() {
        return worldTime;
    }

    public String getWorldName() {
        return worldName;
    }

    // Setters for deserialization
    public void setSpawnX(int spawnX) {
        this.spawnX = spawnX;
    }

    public void setSpawnY(int spawnY) {
        this.spawnY = spawnY;
    }

    public void setSpawnZ(int spawnZ) {
        this.spawnZ = spawnZ;
    }

    public void setWorldSeed(long worldSeed) {
        this.worldSeed = worldSeed;
    }

    public void setWorldTime(long worldTime) {
        this.worldTime = worldTime;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
}