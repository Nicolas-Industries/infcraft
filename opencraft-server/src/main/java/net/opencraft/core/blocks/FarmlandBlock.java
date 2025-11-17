
package net.opencraft.core.blocks;

import net.opencraft.core.blocks.material.Material;
import net.opencraft.core.entity.Entity;
import net.opencraft.core.physics.AABB;
import net.opencraft.core.world.World;
import net.opencraft.server.world.ServerWorld;

import java.util.Random;

public class FarmlandBlock extends Block {

    protected FarmlandBlock(final int blockid) {
        super(blockid, Material.GROUND);
        this.blockIndexInTexture = 87;
        this.setTickOnLoad(true);
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 0.9375f, 1.0f);
        this.setLightOpacity(255);
    }

    @Override
    public AABB getCollisionBoundingBoxFromPool(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        return AABB.getBoundingBoxFromPool(xCoord + 0, yCoord + 0, zCoord + 0, xCoord + 1, yCoord + 1, zCoord + 1);
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
    public int getBlockTextureFromSideAndMetadata(final int textureIndexSlot, final int metadataValue) {
        if (textureIndexSlot == 1 && metadataValue > 0) {
            return this.blockIndexInTexture - 1;
        }
        if (textureIndexSlot == 1) {
            return this.blockIndexInTexture;
        }
        return 2;
    }

    @Override
    public void updateTick(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final Random random) {
        if (random.nextInt(5) == 0) {
            if (this.isWaterNearby(serverWorld, xCoord, yCoord, zCoord)) {
                serverWorld.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 7);
            } else {
                final int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
                if (blockMetadata > 0) {
                    serverWorld.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, blockMetadata - 1);
                } else if (!this.isCropsNearby(serverWorld, xCoord, yCoord, zCoord)) {
                    serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, Block.dirt.blockID);
                }
            }
        }
    }

    @Override
    public void onEntityWalking(final World world, final int xCoord, final int yCoord, final int zCoord, final Entity entity) {
        if (((ServerWorld)world).rand.nextInt(4) == 0) {
            world.setBlockWithNotify(xCoord, yCoord, zCoord, Block.dirt.blockID);
        }
    }

    private boolean isCropsNearby(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        for (int n = 0, i = xCoord - n; i <= xCoord + n; ++i) {
            for (int j = zCoord - n; j <= zCoord + n; ++j) {
                if (serverWorld.getBlockId(i, yCoord + 1, j) == Block.crops.blockID) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWaterNearby(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        for (int i = xCoord - 4; i <= xCoord + 4; ++i) {
            for (int j = yCoord; j <= yCoord + 1; ++j) {
                for (int k = zCoord - 4; k <= zCoord + 4; ++k) {
                    if (serverWorld.getBlockMaterial(i, j, k) == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onNeighborBlockChange(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        super.onNeighborBlockChange(serverWorld, xCoord, yCoord, zCoord, nya4);
        if (serverWorld.getBlockMaterial(xCoord, yCoord + 1, zCoord).isSolid()) {
            serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, Block.dirt.blockID);
        }
    }

    @Override
    public int idDropped(final int blockid, final Random random) {
        return Block.dirt.idDropped(0, random);
    }
}
