package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;

public class PacketEntityDespawn implements IPacket {

    private int entityId;

    public PacketEntityDespawn() {
    }

    public PacketEntityDespawn(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public int getPacketId() {
        return 0x1D; // Standard despawn entity packet ID
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
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
    }

    public int getEntityId() {
        return entityId;
    }
}