package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

public class PacketPlayerDigging implements IPacket {

    private int status;
    private int x;
    private int y;
    private int z;
    private int face;

    public PacketPlayerDigging() {
    }

    public PacketPlayerDigging(int status, int x, int y, int z, int face) {
        this.status = status;
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }

    @Override
    public int getPacketId() {
        return 0x22;
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
        this.status = buffer.readVarInt();
        long pos = buffer.readPosition();
        this.x = PacketBuffer.getPositionX(pos);
        this.y = PacketBuffer.getPositionY(pos);
        this.z = PacketBuffer.getPositionZ(pos);
        this.face = buffer.readByte();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeVarInt(this.status);
        buffer.writePosition(this.x, this.y, this.z);
        buffer.writeByte(this.face);
    }

    public int getStatus() {
        return status;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getFace() {
        return face;
    }
}
