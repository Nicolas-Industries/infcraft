
package net.opencraft.core.blocks;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.world.IBlockAccess;
import net.opencraft.server.world.ServerWorld;

import java.util.Random;

public class SlabBlock extends Block {

    private final boolean blockType;

    public SlabBlock(final int blockid, final boolean doubleSlab) {
        super(blockid, 6, Material.ROCK);
        if (!(this.blockType = doubleSlab)) {
            this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f);
        }
        this.setLightOpacity(255);
    }

    @Override
    public int getBlockTextureFromSide(final int textureIndexSlot) {
        if (textureIndexSlot <= 1) {
            return 6;
        }
        return 5;
    }

    @Override
    public boolean isOpaqueCube() {
        return this.blockType;
    }

    @Override
    public void onNeighborBlockChange(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        if (this != Block.slabSingle) {
            return;
        }
    }

    @Override
    public void onBlockAdded(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        if (this != Block.slabSingle) {
            super.onBlockAdded(serverWorld, xCoord, yCoord, zCoord);
        }
        if (serverWorld.getBlockId(xCoord, yCoord - 1, zCoord) == SlabBlock.slabSingle.blockID) {
            serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
            serverWorld.setBlockWithNotify(xCoord, yCoord - 1, zCoord, Block.slabDouble.blockID);
        }
    }

    @Override
    public int idDropped(final int blockid, final Random random) {
        return Block.slabSingle.blockID;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return this.blockType;
    }

    @Override
    public boolean shouldSideBeRendered(final IBlockAccess blockAccess, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        if (this != Block.slabSingle) {
            super.shouldSideBeRendered(blockAccess, xCoord, yCoord, zCoord, nya4);
        }
        return nya4 == 1 || (super.shouldSideBeRendered(blockAccess, xCoord, yCoord, zCoord, nya4) && (nya4 == 0 || blockAccess.getBlockId(xCoord, yCoord, zCoord) != this.blockID));
    }
}
