
package net.infcraft.core.item;

import net.infcraft.core.blocks.Block;
import net.infcraft.core.entity.EntityPlayer;
import net.infcraft.core.tileentity.TileEntitySign;
import net.infcraft.core.util.Mth;
import net.infcraft.server.world.ServerWorld;

public class ItemSign extends Item {

    public ItemSign(final int itemid) {
        super(itemid);
        this.maxDamage = 64;
        this.maxStackSize = 1;
    }

    @Override
    public boolean onItemUse(final ItemStack hw, final EntityPlayer gi, final ServerWorld fe, final int xCoord, int yCoord, final int zCoord, final int integer7) {
        if (integer7 != 1) {
            return false;
        }
        ++yCoord;
        if (!Block.signPost.canPlaceBlockAt(fe, xCoord, yCoord, zCoord)) {
            return false;
        }
        fe.setBlockWithNotify(xCoord, yCoord, zCoord, Block.signPost.blockID);
        fe.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, Mth.floor_double((gi.rotationYaw + 180.0f) * 16.0f / 360.0f - 0.5) & 0xF);
        --hw.stackSize;
        gi.displayGUIEditSign((TileEntitySign) fe.getBlockTileEntity(xCoord, yCoord, zCoord));
        return true;
    }
}
