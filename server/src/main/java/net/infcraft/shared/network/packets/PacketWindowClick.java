package net.infcraft.shared.network.packets;

import net.infcraft.shared.network.PacketBuffer;
import net.infcraft.core.item.ItemStack;

public class PacketWindowClick implements IPacket {

    private int windowId;
    private int slot;
    private int button;
    private int shift;
    private int actionNumber;
    private ItemStack item;

    public PacketWindowClick() {
    }

    public PacketWindowClick(int windowId, int slot, int button, int shift, ItemStack item, int actionNumber) {
        this.windowId = windowId;
        this.slot = slot;
        this.button = button;
        this.shift = shift;
        this.item = item;
        this.actionNumber = actionNumber;
    }

    @Override
    public int getPacketId() {
        return 0x60;
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
        this.windowId = buffer.readByte();
        this.slot = buffer.readShort();
        this.button = buffer.readByte();
        this.actionNumber = buffer.readShort();
        this.shift = buffer.readByte();
        this.item = buffer.readItemStack();
    }

    @Override
    public void writePacketData(PacketBuffer buffer) {
        buffer.writeByte(this.windowId);
        buffer.writeShort(this.slot);
        buffer.writeByte(this.button);
        buffer.writeShort(this.actionNumber);
        buffer.writeByte(this.shift);
        buffer.writeItemStack(this.item);
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlot() {
        return slot;
    }

    public int getButton() {
        return button;
    }

    public int getShift() {
        return shift;
    }

    public int getActionNumber() {
        return actionNumber;
    }

    public ItemStack getItem() {
        return item;
    }
}
