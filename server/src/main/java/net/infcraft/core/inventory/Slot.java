
package net.infcraft.core.inventory;

import net.infcraft.core.item.ItemStack;

public class Slot {

    public final int slotIndex;
    public final int xPos;
    public final int yPos;
    public final IInventory inventory;

    public Slot(final IInventory inventory, final int slotIndex, final int xPos, final int yPos) {
        this.inventory = inventory;
        this.slotIndex = slotIndex;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void onPickupFromSlot() {
        this.onSlotChanged();
    }

    public boolean isItemValid(final ItemStack hw) {
        return true;
    }

    public ItemStack slotIndex() {
        return this.inventory.getStackInSlot(this.slotIndex);
    }

    public void putStack(final ItemStack hw) {
        this.inventory.setInventorySlotContents(this.slotIndex, hw);
        this.onSlotChanged();
    }

    public int getBackgroundIconIndex() {
        return -1;
    }

    public void onSlotChanged() {
        this.inventory.onInventoryChanged();
    }
}
