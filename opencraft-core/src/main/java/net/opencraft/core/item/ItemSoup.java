
package net.opencraft.core.item;

import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.core.item.Item;
import net.opencraft.core.world.World;

public class ItemSoup extends ItemFood {

    public ItemSoup(final int itemid, final int healAmount) {
        super(itemid, healAmount);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack hw, final World fe, final EntityPlayer gi) {
        super.onItemRightClick(hw, fe, gi);
        return new ItemStack(Item.bowlEmpty);
    }
}
