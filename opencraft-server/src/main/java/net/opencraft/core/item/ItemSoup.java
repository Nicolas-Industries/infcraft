
package net.opencraft.core.item;

import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.server.world.ServerWorld;

public class ItemSoup extends ItemFood {

    public ItemSoup(final int itemid, final int healAmount) {
        super(itemid, healAmount);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack hw, final ServerWorld fe, final EntityPlayer gi) {
        super.onItemRightClick(hw, fe, gi);
        return new ItemStack(Item.bowlEmpty);
    }
}
