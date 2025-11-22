package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;
import net.infcraft.core.item.ItemStack;

public class PacketSpawnItem implements IPacket {

    private int entityId;
    private double x;
    private double y;
    private double z;
    private double vx;
    private double vy;
    private double vz;
    private ItemStack itemStack;

    public PacketSpawnItem() {
    }

    public PacketSpawnItem(int entityId, double x, double y, double z, double vx, double vy, double vz,
            ItemStack itemStack) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.itemStack = itemStack;
    }

    @Override
    public int getPacketId() {
        return 0x15; // 21 - Pickup Spawn
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
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.vx = buffer.readDouble();
        this.vy = buffer.readDouble();
        this.vz = buffer.readDouble();

        // Read item stack
        short itemId = buffer.readShort();
        if (itemId >= 0) {
            byte count = buffer.readByte();
            short damage = buffer.readShort();
            this.itemStack = new ItemStack(itemId, count, damage);
        }
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        buffer.writeDouble(this.vx);
        buffer.writeDouble(this.vy);
        buffer.writeDouble(this.vz);

        // Write item stack
        if (this.itemStack != null) {
            buffer.writeShort((short) this.itemStack.itemID);
            buffer.writeByte((byte) this.itemStack.stackSize);
            buffer.writeShort((short) this.itemStack.itemDamage);
        } else {
            buffer.writeShort((short) -1);
        }
    }

    public int getEntityId() {
        return entityId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getVz() {
        return vz;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
