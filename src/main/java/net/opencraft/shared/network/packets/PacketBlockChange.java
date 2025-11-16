package net.opencraft.shared.network.packets;

/**
 * Packet for block changes (placement, destruction)
 */
public class PacketBlockChange implements IPacket {
    private int x, y, z;
    private int blockId;
    private int metadata;

    public PacketBlockChange() {
        // Default constructor for deserialization
    }

    public PacketBlockChange(int x, int y, int z, int blockId, int metadata) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
        this.metadata = metadata;
    }

    @Override
    public int getPacketId() {
        return 0x06; // Block change packet ID
    }

    @Override
    public boolean isServerToClient() {
        return true;  // Server sends block changes to clients
    }

    @Override
    public boolean isClientToServer() {
        return false; // This is server to client
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockId() { return blockId; }
    public int getMetadata() { return metadata; }

    // Setters for deserialization
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setZ(int z) { this.z = z; }
    public void setBlockId(int blockId) { this.blockId = blockId; }
    public void setMetadata(int metadata) { this.metadata = metadata; }
}