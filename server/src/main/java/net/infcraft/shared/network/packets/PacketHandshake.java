package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;

public class PacketHandshake implements IPacket {

    private int protocolVersion;
    private int nextState;
    private String username;

    public PacketHandshake() {
    }

    public PacketHandshake(int protocolVersion, int nextState, String username) {
        this.protocolVersion = protocolVersion;
        this.nextState = nextState;
        this.username = username;
    }

    @Override
    public int getPacketId() {
        return 0x00;
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
        this.protocolVersion = buffer.readVarInt();
        this.nextState = buffer.readVarInt();
        this.username = buffer.readString();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeVarInt(this.protocolVersion);
        buffer.writeVarInt(this.nextState);
        buffer.writeString(this.username);
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public int getNextState() {
        return nextState;
    }

    public String getUsername() {
        return username;
    }
}
