
package net.infcraft.core.item;

import net.infcraft.core.entity.EntityPlayer;
import net.infcraft.server.world.ServerWorld;

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
