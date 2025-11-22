
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.core.physics.AABB;
import net.infcraft.core.world.World;
import net.infcraft.server.world.ServerWorld;

import java.util.Random;

public class LadderBlock extends Block {

    protected LadderBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture, Material.REDSTONE);
    }

    @Override
    public AABB getCollisionBoundingBoxFromPool(final World world, final int xCoord, final int yCoord, final int zCoord) {
        if (!(world instanceof ServerWorld serverWorld)) {
            System.out.println("WARNING:LadderBlock: Attempt to use getCollisionBoundingBoxFromPool from Client!");
            return null;
        }

        // conversion!
        final int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        final float n = 0.125f;
        if (blockMetadata == 2) {
            this.setShape(0.0f, 0.0f, 1.0f - n, 1.0f, 1.0f, 1.0f);
        }
        if (blockMetadata == 3) {
            this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, n);
        }
        if (blockMetadata == 4) {
            this.setShape(1.0f - n, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
        if (blockMetadata == 5) {
            this.setShape(0.0f, 0.0f, 0.0f, n, 1.0f, 1.0f);
        }
        return super.getCollisionBoundingBoxFromPool(serverWorld, xCoord, yCoord, zCoord);
    }

    @Override
    public AABB getSelectedBoundingBoxFromPool(World world, final int xCoord, final int yCoord, final int zCoord) {
        final int blockMetadata = world.getBlockMetadata(xCoord, yCoord, zCoord);
        final float n = 0.125f;
        if (blockMetadata == 2) {
            this.setShape(0.0f, 0.0f, 1.0f - n, 1.0f, 1.0f, 1.0f);
        }
        if (blockMetadata == 3) {
            this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, n);
        }
        if (blockMetadata == 4) {
            this.setShape(1.0f - n, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
        if (blockMetadata == 5) {
            this.setShape(0.0f, 0.0f, 0.0f, n, 1.0f, 1.0f);
        }
        return super.getSelectedBoundingBoxFromPool(world, xCoord, yCoord, zCoord);
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
        return 8;
    }

    @Override
    public boolean canPlaceBlockAt(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        return serverWorld.isBlockNormalCube(xCoord - 1, yCoord, zCoord) || serverWorld.isBlockNormalCube(xCoord + 1, yCoord, zCoord) || serverWorld.isBlockNormalCube(xCoord, yCoord, zCoord - 1) || serverWorld.isBlockNormalCube(xCoord, yCoord, zCoord + 1);
    }

    @Override
    public void onBlockPlaced(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        if ((blockMetadata == 0 || nya4 == 2) && serverWorld.isBlockNormalCube(xCoord, yCoord, zCoord + 1)) {
            blockMetadata = 2;
        }
        if ((blockMetadata == 0 || nya4 == 3) && serverWorld.isBlockNormalCube(xCoord, yCoord, zCoord - 1)) {
            blockMetadata = 3;
        }
        if ((blockMetadata == 0 || nya4 == 4) && serverWorld.isBlockNormalCube(xCoord + 1, yCoord, zCoord)) {
            blockMetadata = 4;
        }
        if ((blockMetadata == 0 || nya4 == 5) && serverWorld.isBlockNormalCube(xCoord - 1, yCoord, zCoord)) {
            blockMetadata = 5;
        }
        serverWorld.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, blockMetadata);
    }

    @Override
    public void onNeighborBlockChange(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        final int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean b = false;
        if (blockMetadata == 2 && serverWorld.isBlockNormalCube(xCoord, yCoord, zCoord + 1)) {
            b = true;
        }
        if (blockMetadata == 3 && serverWorld.isBlockNormalCube(xCoord, yCoord, zCoord - 1)) {
            b = true;
        }
        if (blockMetadata == 4 && serverWorld.isBlockNormalCube(xCoord + 1, yCoord, zCoord)) {
            b = true;
        }
        if (blockMetadata == 5 && serverWorld.isBlockNormalCube(xCoord - 1, yCoord, zCoord)) {
            b = true;
        }
        if (!b) {
            this.dropBlockAsItem(serverWorld, xCoord, yCoord, zCoord, blockMetadata);
            serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
        }
        super.onNeighborBlockChange(serverWorld, xCoord, yCoord, zCoord, nya4);
    }

    @Override
    public int quantityDropped(final Random random) {
        return 1;
    }
}
