
package net.opencraft.core.item;

import net.opencraft.core.entity.Entity;
import net.opencraft.core.entity.EntityArrow;
import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.server.world.ServerWorld;

public class ItemBow extends Item {

    public ItemBow(final int itemid) {
        super(itemid);
        this.maxStackSize = 1;
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack hw, final ServerWorld fe, final EntityPlayer gi) {
        if (gi.inventory.consumeInventoryItem(Item.arrow.shiftedIndex)) {
            fe.playSound((Entity) gi, "random.bow", 1.0f, 1.0f / (ItemBow.itemRand.nextFloat() * 0.4f + 0.8f));
            fe.entityJoinedWorld((Entity) new EntityArrow(fe, gi));
        }
        return hw;
    }
}
