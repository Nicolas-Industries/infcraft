
package net.opencraft.core.blocks;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.tileentity.TileEntity;
import net.opencraft.core.tileentity.TileEntityMobSpawner;

public class SpawnerBlock extends ContainerBlock {

    protected SpawnerBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture, Material.ROCK);
    }

    @Override
    protected TileEntity getBlockEntity() {
        return new TileEntityMobSpawner();
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
}
