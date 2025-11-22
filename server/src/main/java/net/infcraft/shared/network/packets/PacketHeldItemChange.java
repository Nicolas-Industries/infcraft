package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;

public class PacketHeldItemChange implements IPacket {

    private int slot;

    public PacketHeldItemChange() {
    }

    public PacketHeldItemChange(int slot) {
        this.slot = slot;
    }

    @Override
    public int getPacketId() {
        return 0x24;
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
        this.slot = buffer.readVarInt();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeVarInt(this.slot);
    }

    public int getSlot() {
        return slot;
    }
}
