package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

/**
 * Packet for player position and rotation updates
 */
public class PacketPlayerPosition implements IPacket {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;

    public PacketPlayerPosition() {
        // Default constructor for deserialization
    }

    public PacketPlayerPosition(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x00; // Player position packet ID
    }

    @Override
    public boolean isServerToClient() {
        return true; // Server can send position corrections to client
    }

    @Override
    public boolean isClientToServer() {
        return true; // Client can send position updates to server
    }

    @Override
    public void readPacketData(PacketBuffer buffer) throws java.io.IOException {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        yaw = buffer.readFloat();
        pitch = buffer.readFloat();
        onGround = buffer.readBoolean();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) throws java.io.IOException {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(onGround);
    }

    // Getters
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

    public boolean isOnGround() {
        return onGround;
    }

    // Setters for deserialization
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}