package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;

/**
 * Packet for block changes (placement, destruction)
 */
public class PacketBlockChange implements IPacket {
    private int x, y, z;
    private int blockId;

    public PacketBlockChange() {
        // Default constructor for deserialization
    }

    public PacketBlockChange(int x, int y, int z, int blockId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
    }

    @Override
    public int getPacketId() {
        return 0x21;
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
        long pos = buffer.readPosition();
        this.x = PacketBuffer.getPositionX(pos);
        this.y = PacketBuffer.getPositionY(pos);
        this.z = PacketBuffer.getPositionZ(pos);
        this.blockId = buffer.readVarInt();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writePosition(x, y, z);
        buffer.writeVarInt(blockId);
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

    public int getBlockId() {
        return blockId;
    }
}