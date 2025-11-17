
package net.opencraft.core.item;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.server.world.ServerWorld;

public class ItemSeeds extends Item {

    private int blockType;

    public ItemSeeds(final int itemid, final int blockType) {
        super(itemid);
        this.blockType = blockType;
    }

    @Override
    public boolean onItemUse(final ItemStack hw, final EntityPlayer gi, final ServerWorld fe, final int xCoord, final int yCoord, final int zCoord, final int integer7) {
        if (integer7 != 1) {
            return false;
        }
        if (fe.getBlockId(xCoord, yCoord, zCoord) == Block.tilledField.blockID) {
            fe.setBlockWithNotify(xCoord, yCoord + 1, zCoord, this.blockType);
            --hw.stackSize;
            return true;
        }
        return false;
    }
}
