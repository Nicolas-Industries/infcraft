
package net.opencraft.core.blocks;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.physics.AABB;
import net.opencraft.core.world.World;

import java.util.Random;

public class GearsBlock extends Block {

    protected GearsBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture, Material.REDSTONE);
    }

    @Override
    public AABB getCollisionBoundingBoxFromPool(final World world, final int xCoord, final int yCoord, final int zCoord) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 5;
    }

    @Override
    public int quantityDropped(final Random random) {
        return 1;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }
}
