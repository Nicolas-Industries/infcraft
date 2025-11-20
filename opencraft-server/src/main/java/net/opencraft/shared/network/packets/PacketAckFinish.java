package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

public class PacketAckFinish implements IPacket {

    public PacketAckFinish() {
    }

    @Override
    public int getPacketId() {
        return 0x01;
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
        // No payload
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        // No payload
    }
}
