package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;

public class PacketKeepAlive implements IPacket {

    private long payload;

    public PacketKeepAlive() {
    }

    public PacketKeepAlive(long payload) {
        this.payload = payload;
    }

    @Override
    public int getPacketId() {
        return 0x00;
    }

    @Override
    public boolean isServerToClient() {
        return true;
    }

    @Override
    public boolean isClientToServer() {
        return true;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) {
        this.payload = buffer.readLong();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeLong(this.payload);
    }

    public long getPayload() {
        return payload;
    }
}
