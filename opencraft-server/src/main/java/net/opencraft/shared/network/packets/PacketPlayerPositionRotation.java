package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

public class PacketPlayerPositionRotation implements IPacket {

    private float x;
    private float y;
    private float z;
    private float pitch;
    private float yaw;
    private boolean onGround;

    public PacketPlayerPositionRotation() {
    }

    public PacketPlayerPositionRotation(float x, float y, float z, float pitch, float yaw, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x33;
    }

    @Override
    public boolean isServerToClient() {
        return false;
    }

    @Override
    public boolean isClientToServer() {
        return true;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) {
        this.x = buffer.readFloat();
        this.y = buffer.readFloat();
        this.z = buffer.readFloat();
        this.pitch = buffer.readFloat();
        this.yaw = buffer.readFloat();
        this.onGround = buffer.readBoolean();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.y);
        buffer.writeFloat(this.z);
        buffer.writeFloat(this.pitch);
        buffer.writeFloat(this.yaw);
        buffer.writeBoolean(this.onGround);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public boolean isOnGround() {
        return onGround;
    }
}
