package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;
import java.util.UUID;

public class PacketSpawnEntity implements IPacket {

    private int entityId;
    private UUID uuid;
    private int type;
    private float x;
    private float y;
    private float z;
    private int pitch;
    private int yaw;
    private String username;

    public PacketSpawnEntity() {
    }

    public PacketSpawnEntity(int entityId, UUID uuid, int type, float x, float y, float z, int pitch, int yaw,
            String username) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.username = username;
    }

    @Override
    public int getPacketId() {
        return 0x30;
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
        this.uuid = buffer.readUUID();
        this.type = buffer.readVarInt();
        this.x = buffer.readFloat();
        this.y = buffer.readFloat();
        this.z = buffer.readFloat();
        this.pitch = buffer.readByte() & 0xFF;
        this.yaw = buffer.readByte() & 0xFF;
        this.username = buffer.readString();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeUUID(this.uuid);
        buffer.writeVarInt(this.type);
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.y);
        buffer.writeFloat(this.z);
        buffer.writeByte(this.pitch);
        buffer.writeByte(this.yaw);
        buffer.writeString(this.username);
    }

    public int getEntityId() {
        return entityId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getPitch() {
        return pitch;
    }

    public int getYaw() {
        return yaw;
    }

    public String getUsername() {
        return username;
    }
}
