
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.server.world.ServerWorld;

public class StaticLiquidBlock extends LiquidBlock {

    protected StaticLiquidBlock(final int blockid, final Material material) {
        super(blockid, material);
        this.setTickOnLoad(false);
    }

    @Override
    public void onNeighborBlockChange(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        super.onNeighborBlockChange(serverWorld, xCoord, yCoord, zCoord, nya4);
        if (serverWorld.getBlockId(xCoord, yCoord, zCoord) == this.blockID) {
            this.updateTick(serverWorld, xCoord, yCoord, zCoord);
        }
    }

    private void updateTick(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        final int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        serverWorld.editingBlocks = true;
        serverWorld.setBlockAndMetadata(xCoord, yCoord, zCoord, this.blockID - 1, blockMetadata);
        serverWorld.markBlocksDirty(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        serverWorld.scheduleBlockUpdate(xCoord, yCoord, zCoord, this.blockID - 1);
        serverWorld.editingBlocks = false;
    }
}
