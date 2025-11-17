
package net.opencraft.core.inventory;

import net.opencraft.core.item.ItemStack;

public class SlotCrafting extends Slot {

    private final IInventory craftMatrix;

    public SlotCrafting(final IInventory inventory, final IInventory craftMatrix, final int slotIndex, final int x, final int y) {
        super(inventory, slotIndex, x, y);
        this.craftMatrix = craftMatrix;
    }

    @Override
    public boolean isItemValid(final ItemStack hw) {
        return false;
    }

    @Override
    public void onPickupFromSlot() {
        for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i) {
            if (this.craftMatrix.getStackInSlot(i) != null) {
                this.craftMatrix.decrStackSize(i, 1);
            }
        }
    }
}
