package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;
import net.infcraft.core.item.ItemStack;

public class PacketSetSlot implements IPacket {

    private int windowId;
    private int slot;
    private ItemStack item;

    public PacketSetSlot() {
    }

    public PacketSetSlot(int windowId, int slot, ItemStack item) {
        this.windowId = windowId;
        this.slot = slot;
        this.item = item;
    }

    @Override
    public int getPacketId() {
        return 0x61;
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
        this.windowId = buffer.readByte();
        this.slot = buffer.readShort();
        this.item = buffer.readItemStack();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeByte(this.windowId);
        buffer.writeShort(this.slot);
        buffer.writeItemStack(this.item);
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItem() {
        return item;
    }
}
