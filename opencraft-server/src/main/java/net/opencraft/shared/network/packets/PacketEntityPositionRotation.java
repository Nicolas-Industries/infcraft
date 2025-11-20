package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

/**
 * Packet to send entity position and rotation update from server to client
 */
public class PacketEntityPositionRotation implements IPacket {

    private int entityId;
    private double x, y, z;
    private float yaw, pitch;

    // Default constructor for deserialization
    public PacketEntityPositionRotation() {}

    public PacketEntityPositionRotation(int entityId, double x, double y, double z, float yaw, float pitch) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) throws java.io.IOException {
        entityId = buffer.readInt();
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        yaw = buffer.readFloat();
        pitch = buffer.readFloat();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) throws java.io.IOException {
        buffer.writeInt(entityId);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
    }

    @Override
    public int getPacketId() {
        return 0x20; // Entity position and rotation packet ID
    }

    // Getters
    public int getEntityId() { return entityId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    @Override
    public boolean isServerToClient() {
        return true; // This packet goes from server to client
    }

    @Override
    public boolean isClientToServer() {
        return false; // This packet does not go from client to server
    }
}