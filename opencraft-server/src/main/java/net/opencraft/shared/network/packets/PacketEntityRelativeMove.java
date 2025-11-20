package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;

public class PacketEntityRelativeMove implements IPacket {

    private int entityId;
    private short deltaX;
    private short deltaY;
    private short deltaZ;
    private boolean onGround;

    public PacketEntityRelativeMove() {
    }

    public PacketEntityRelativeMove(int entityId, short deltaX, short deltaY, short deltaZ, boolean onGround) {
        this.entityId = entityId;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x32;
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
        this.entityId = buffer.readVarInt();
        this.deltaX = buffer.readShort();
        this.deltaY = buffer.readShort();
        this.deltaZ = buffer.readShort();
        this.onGround = buffer.readBoolean();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeShort(this.deltaX);
        buffer.writeShort(this.deltaY);
        buffer.writeShort(this.deltaZ);
        buffer.writeBoolean(this.onGround);
    }

    public int getEntityId() {
        return entityId;
    }

    public short getDeltaX() {
        return deltaX;
    }

    public short getDeltaY() {
        return deltaY;
    }

    public short getDeltaZ() {
        return deltaZ;
    }

    public boolean isOnGround() {
        return onGround;
    }
}
