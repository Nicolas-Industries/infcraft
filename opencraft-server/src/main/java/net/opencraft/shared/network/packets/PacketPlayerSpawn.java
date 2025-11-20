package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

/**
 * Packet to notify clients about player spawn/creation
 */
public class PacketPlayerSpawn implements IPacket {

    private int entityId;
    private double x, y, z;
    private float yaw, pitch;
    private String username;

    // Default constructor for deserialization
    public PacketPlayerSpawn() {
    }

    public PacketPlayerSpawn(int entityId, double x, double y, double z, float yaw, float pitch, String username) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.username = username;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) throws java.io.IOException {
        entityId = buffer.readInt();
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        yaw = buffer.readFloat();
        pitch = buffer.readFloat();
        username = buffer.readString();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) throws java.io.IOException {
        buffer.writeInt(entityId);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeString(username);
    }

    @Override
    public int getPacketId() {
        return 0x14; // Player spawn packet ID (20)
    }

    // Getters
    public int getEntityId() {
        return entityId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isServerToClient() {
        return true; // This packet goes from server to client
    }

    @Override
    public boolean isClientToServer() {
        return false; // This packet does not go from client to server
    }
}