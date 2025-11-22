
package net.infcraft.core.inventory;

import net.infcraft.core.item.ItemArmor;
import net.infcraft.core.item.ItemStack;

public class SlotArmor extends Slot {

    public final /* synthetic */ int armorType;
    public SlotArmor(IInventory kd2, int n, int n2, int n3, int n4) {
        super(kd2, n, n2, n3);
        this.armorType = n4;
    }

    public boolean isItemValid(ItemStack hw2) {
        if (hw2.getItem() instanceof ItemArmor) {
            return ((ItemArmor) hw2.getItem()).armorType == this.armorType;
        }
        return false;
    }

    public int getBackgroundIconIndex() {
        return 15 + this.armorType * 16;
    }
}
