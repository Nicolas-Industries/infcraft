
package net.opencraft.core.blocks;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.world.IBlockAccess;

public class LeavesBaseBlock extends Block {

    protected boolean graphicsLevel;

    protected LeavesBaseBlock(final int blockid, final int blockIndexInTexture, final Material material, final boolean boolean4) {
        super(blockid, blockIndexInTexture, material);
        this.graphicsLevel = boolean4;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(final IBlockAccess blockAccess, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        final int blockId = blockAccess.getBlockId(xCoord, yCoord, zCoord);
        return (this.graphicsLevel || blockId != this.blockID) && super.shouldSideBeRendered(blockAccess, xCoord, yCoord, zCoord, nya4);
    }
}
