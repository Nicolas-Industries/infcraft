package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

public class PacketLoginSuccess implements IPacket {

    private int entityId;
    private float spawnX;
    private float spawnY;
    private float spawnZ;
    private String username;

    public PacketLoginSuccess() {
    }

    public PacketLoginSuccess(int entityId, float spawnX, float spawnY, float spawnZ, String username) {
        this.entityId = entityId;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.username = username;
    }

    @Override
    public int getPacketId() {
        return 0x01;
    }

    @Override
    public boolean isServerToClient() {
        return true;
    }

    @Override
    public boolean isClientToServer() {
        return false;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) {
        this.entityId = buffer.readInt();
        this.spawnX = buffer.readFloat();
        this.spawnY = buffer.readFloat();
        this.spawnZ = buffer.readFloat();
        this.username = buffer.readString();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeFloat(this.spawnX);
        buffer.writeFloat(this.spawnY);
        buffer.writeFloat(this.spawnZ);
        buffer.writeString(this.username);
    }

    public int getEntityId() {
        return entityId;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public float getSpawnZ() {
        return spawnZ;
    }

    public String getUsername() {
        return username;
    }
}
