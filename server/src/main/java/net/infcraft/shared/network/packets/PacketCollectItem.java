package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;

public class PacketCollectItem implements IPacket {

    private int collectedEntityId;
    private int collectorEntityId;

    public PacketCollectItem() {
    }

    public PacketCollectItem(int collectedEntityId, int collectorEntityId) {
        this.collectedEntityId = collectedEntityId;
        this.collectorEntityId = collectorEntityId;
    }

    @Override
    public int getPacketId() {
        return 0x16; // 22 - Collect Item
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
        this.collectedEntityId = buffer.readInt();
        this.collectorEntityId = buffer.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeInt(this.collectedEntityId);
        buffer.writeInt(this.collectorEntityId);
    }

    public int getCollectedEntityId() {
        return collectedEntityId;
    }

    public int getCollectorEntityId() {
        return collectorEntityId;
    }
}
