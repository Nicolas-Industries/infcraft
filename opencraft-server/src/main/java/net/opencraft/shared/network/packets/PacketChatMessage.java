package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

public class PacketChatMessage implements IPacket {

    private String message;
    private int type; // 0 = Chat, 1 = System Message

    public PacketChatMessage() {
    }

    public PacketChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    @Override
    public int getPacketId() {
        return 0x02;
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
        this.message = buffer.readString();
        this.type = buffer.readByte();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeString(this.message);
        buffer.writeByte(this.type);
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }
}
