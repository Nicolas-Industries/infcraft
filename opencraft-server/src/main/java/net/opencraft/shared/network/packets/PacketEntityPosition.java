package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

public class PacketEntityPosition implements IPacket {

    private int entityId;
    private float x;
    private float y;
    private float z;
    private boolean onGround;

    public PacketEntityPosition() {
    }

    public PacketEntityPosition(int entityId, float x, float y, float z, boolean onGround) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x31;
    }

    @Override
    public void readPacketData(PacketBuffer buffer) {
        this.entityId = buffer.readVarInt();
        this.x = buffer.readFloat();
        this.y = buffer.readFloat();
        this.z = buffer.readFloat();
        this.onGround = buffer.readBoolean();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.y);
        buffer.writeFloat(this.z);
        buffer.writeBoolean(this.onGround);
    }

    @Override
    public boolean isServerToClient() {
        return true;
    }

    @Override
    public boolean isClientToServer() {
        return false; // This packet does not go from client to server
    }
}