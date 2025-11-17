
package net.opencraft.core.item;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.entity.Entity;
import net.opencraft.core.entity.EntityMinecart;
import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.server.world.ServerWorld;

public class ItemMinecart extends Item {

    public ItemMinecart(final int itemid) {
        super(itemid);
        this.maxStackSize = 1;
    }

    @Override
    public boolean onItemUse(final ItemStack hw, final EntityPlayer gi, final ServerWorld fe, final int xCoord, final int yCoord, final int zCoord, final int integer7) {
        if (fe.getBlockId(xCoord, yCoord, zCoord) == Block.rail.blockID) {
            fe.entityJoinedWorld((Entity) new EntityMinecart(fe, xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f));
            --hw.stackSize;
            return true;
        }
        return false;
    }
}
