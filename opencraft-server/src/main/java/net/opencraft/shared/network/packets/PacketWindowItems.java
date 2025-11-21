package net.opencraft.shared.network.packets;

import net.opencraft.shared.network.PacketBuffer;
import net.opencraft.core.item.ItemStack;
import java.util.List;

public class PacketWindowItems implements IPacket {

    private int windowId;
    private ItemStack[] items;

    public PacketWindowItems() {
    }

    public PacketWindowItems(int windowId, List<ItemStack> items) {
        this.windowId = windowId;
        this.items = new ItemStack[items.size()];
        for (int i = 0; i < items.size(); i++) {
            this.items[i] = items.get(i);
        }
    }

    public PacketWindowItems(int windowId, ItemStack[] items) {
        this.windowId = windowId;
        this.items = items;
    }

    @Override
    public int getPacketId() {
        return 0x62;
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
        int count = buffer.readShort();
        this.items = new ItemStack[count];
        for (int i = 0; i < count; i++) {
            this.items[i] = buffer.readItemStack();
        }
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeByte(this.windowId);
        buffer.writeShort(this.items.length);
        for (ItemStack item : this.items) {
            buffer.writeItemStack(item);
        }
    }

    public int getWindowId() {
        return windowId;
    }

    public ItemStack[] getItems() {
        return items;
    }
}
