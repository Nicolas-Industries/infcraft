package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;

public class PacketBlockPlacement implements IPacket {

    private int x;
    private int y;
    private int z;
    private int face;
    private int hand;

    public PacketBlockPlacement() {
    }

    public PacketBlockPlacement(int x, int y, int z, int face, int hand) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
        this.hand = hand;
    }

    @Override
    public int getPacketId() {
        return 0x23;
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
        long pos = buffer.readPosition();
        this.x = PacketBuffer.getPositionX(pos);
        this.y = PacketBuffer.getPositionY(pos);
        this.z = PacketBuffer.getPositionZ(pos);
        this.face = buffer.readVarInt();
        this.hand = buffer.readVarInt();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writePosition(this.x, this.y, this.z);
        buffer.writeVarInt(this.face);
        buffer.writeVarInt(this.hand);
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

    public int getHand() {
        return hand;
    }
}
