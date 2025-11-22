
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.core.tileentity.TileEntity;
import net.infcraft.core.tileentity.TileEntityMobSpawner;

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
