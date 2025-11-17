
package net.opencraft.core.item;

import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.server.world.ServerWorld;

public class ItemFood extends Item {

    private int healAmount;

    public ItemFood(final int itemid, final int healAmount) {
        super(itemid);
        this.healAmount = healAmount;
        this.maxStackSize = 64;
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack hw, final ServerWorld fe, final EntityPlayer gi) {
        if (gi.health <= 20) {
            --hw.stackSize;
            gi.heal(this.healAmount);
            return hw;
        } else {
            return hw;
        }
    }
}
