
package net.opencraft.core.item;

import net.opencraft.core.entity.EntityLiving;
import net.opencraft.core.entity.EntityPig;
import net.opencraft.core.item.Item;

public class ItemSaddle extends Item {

    public ItemSaddle(final int itemid) {
        super(itemid);
        this.maxStackSize = 1;
        this.maxDamage = 64;
    }

    @Override
    public void saddleEntity(final ItemStack hw, final EntityLiving ka) {
        if (ka instanceof EntityPig) {
            final EntityPig entityPig = (EntityPig) ka;
            if (!entityPig.getSaddled) {
                entityPig.getSaddled = true;
                --hw.stackSize;
            }
        }
    }

    @Override
    public void hitEntity(final ItemStack hw, final EntityLiving ka) {
        this.saddleEntity(hw, ka);
    }
}
