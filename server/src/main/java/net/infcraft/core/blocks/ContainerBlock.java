
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.core.tileentity.TileEntity;
import net.infcraft.server.world.ServerWorld;

public abstract class ContainerBlock extends Block {

    protected ContainerBlock(final int blockid, final Material material) {
        super(blockid, material);
    }

    protected ContainerBlock(final int blockid, final int textureIndexSlot, final Material material) {
        super(blockid, textureIndexSlot, material);
    }

    @Override
    public void onBlockAdded(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        super.onBlockAdded(serverWorld, xCoord, yCoord, zCoord);
        serverWorld.setBlockTileEntity(xCoord, yCoord, zCoord, this.getBlockEntity());
    }

    @Override
    public void onBlockRemoval(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        super.onBlockRemoval(serverWorld, xCoord, yCoord, zCoord);
        serverWorld.removeBlockTileEntity(xCoord, yCoord, zCoord);
    }

    protected abstract TileEntity getBlockEntity();
}
