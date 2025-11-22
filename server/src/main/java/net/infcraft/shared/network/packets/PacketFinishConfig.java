package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;

public class PacketFinishConfig implements IPacket {

    public PacketFinishConfig() {
    }

    @Override
    public int getPacketId() {
        return 0x01;
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
        // No payload
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        // No payload
    }
}
