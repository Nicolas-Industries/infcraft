
package net.opencraft.core.blocks;

import net.opencraft.core.entity.Entity;
import net.opencraft.core.entity.EntityPlayer;
import net.opencraft.core.physics.AABB;
import net.opencraft.core.util.Vec3;
import net.opencraft.core.world.IBlockAccess;
import net.opencraft.core.world.World;
import net.opencraft.server.world.ServerWorld;

import java.util.List;
import java.util.Random;

public class StairBlock extends Block {

    private final Block modelBlock;

    protected StairBlock(final int blockid, final Block block) {
        super(blockid, block.blockIndexInTexture, block.blockMaterial);
        this.modelBlock = block;
        this.setHardness(block.blockHardness);
        this.setResistance(block.blockResistance / 3.0f);
        this.setStepSound(block.stepSound);
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
        return 10;
    }

    @Override
    public boolean shouldSideBeRendered(final IBlockAccess blockAccess, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        return super.shouldSideBeRendered(blockAccess, xCoord, yCoord, zCoord, nya4);
    }

    @Override
    public void getCollidingBoundingBoxes(ServerWorld serverWorld, int xCoord, int yCoord, final int zCoord, AABB aabb, List<AABB> arrayList) {
        final int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
        if (blockMetadata == 0) {
            this.setShape(0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 1.0f);
            super.getCollidingBoundingBoxes(serverWorld, xCoord, yCoord, zCoord, aabb, arrayList);
            this.setShape(0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            super.getCollidingBoundingBoxes(serverWorld, xCoord, yCoord, zCoord, aabb, arrayList);
        } else if (blockMetadata == 1) {
            this.setShape(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f);
            super.getCollidingBoundingBoxes(serverWorld, xCoord, yCoord, zCoord, aabb, arrayList);
            this.setShape(0.5f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f);
            super.getCollidingBoundingBoxes(serverWorld, xCoord, yCoord, zCoord, aabb, arrayList);
        } else if (blockMetadata == 2) {
            this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 0.5f);
            super.getCollidingBoundingBoxes(serverWorld, xCoord, yCoord, zCoord, aabb, arrayList);
            this.setShape(0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f);
            super.getCollidingBoundingBoxes(serverWorld, xCoord, yCoord, zCoord, aabb, arrayList);
        } else if (blockMetadata == 3) {
            this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
            super.getCollidingBoundingBoxes(serverWorld, xCoord, yCoord, zCoord, aabb, arrayList);
            this.setShape(0.0f, 0.0f, 0.5f, 1.0f, 0.5f, 1.0f);
            super.getCollidingBoundingBoxes(serverWorld, xCoord, yCoord, zCoord, aabb, arrayList);
        }
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void onNeighborBlockChange(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        if (serverWorld.getBlockMaterial(xCoord, yCoord + 1, zCoord).isSolid()) {
            serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, this.modelBlock.blockID);
        } else {
            this.g(serverWorld, xCoord, yCoord, zCoord);
            this.g(serverWorld, xCoord + 1, yCoord - 1, zCoord);
            this.g(serverWorld, xCoord - 1, yCoord - 1, zCoord);
            this.g(serverWorld, xCoord, yCoord - 1, zCoord - 1);
            this.g(serverWorld, xCoord, yCoord - 1, zCoord + 1);
            this.g(serverWorld, xCoord + 1, yCoord + 1, zCoord);
            this.g(serverWorld, xCoord - 1, yCoord + 1, zCoord);
            this.g(serverWorld, xCoord, yCoord + 1, zCoord - 1);
            this.g(serverWorld, xCoord, yCoord + 1, zCoord + 1);
        }
        this.modelBlock.onNeighborBlockChange(serverWorld, xCoord, yCoord, zCoord, nya4);
    }

    private void g(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        if (!this.i(serverWorld, xCoord, yCoord, zCoord)) {
            return;
        }
        int metadataValue = -1;
        if (this.i(serverWorld, xCoord + 1, yCoord + 1, zCoord)) {
            metadataValue = 0;
        }
        if (this.i(serverWorld, xCoord - 1, yCoord + 1, zCoord)) {
            metadataValue = 1;
        }
        if (this.i(serverWorld, xCoord, yCoord + 1, zCoord + 1)) {
            metadataValue = 2;
        }
        if (this.i(serverWorld, xCoord, yCoord + 1, zCoord - 1)) {
            metadataValue = 3;
        }
        if (metadataValue < 0) {
            if (this.h(serverWorld, xCoord + 1, yCoord, zCoord) && !this.h(serverWorld, xCoord - 1, yCoord, zCoord)) {
                metadataValue = 0;
            }
            if (this.h(serverWorld, xCoord - 1, yCoord, zCoord) && !this.h(serverWorld, xCoord + 1, yCoord, zCoord)) {
                metadataValue = 1;
            }
            if (this.h(serverWorld, xCoord, yCoord, zCoord + 1) && !this.h(serverWorld, xCoord, yCoord, zCoord - 1)) {
                metadataValue = 2;
            }
            if (this.h(serverWorld, xCoord, yCoord, zCoord - 1) && !this.h(serverWorld, xCoord, yCoord, zCoord + 1)) {
                metadataValue = 3;
            }
        }
        if (metadataValue < 0) {
            if (this.i(serverWorld, xCoord - 1, yCoord - 1, zCoord)) {
                metadataValue = 0;
            }
            if (this.i(serverWorld, xCoord + 1, yCoord - 1, zCoord)) {
                metadataValue = 1;
            }
            if (this.i(serverWorld, xCoord, yCoord - 1, zCoord - 1)) {
                metadataValue = 2;
            }
            if (this.i(serverWorld, xCoord, yCoord - 1, zCoord + 1)) {
                metadataValue = 3;
            }
        }
        if (metadataValue >= 0) {
            serverWorld.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadataValue);
        }
    }

    private boolean h(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        return serverWorld.getBlockMaterial(xCoord, yCoord, zCoord).isSolid();
    }

    private boolean i(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        final int blockId = serverWorld.getBlockId(xCoord, yCoord, zCoord);
        return blockId != 0 && Block.blocksList[blockId].getRenderType() == 10;
    }

    @Override
    public void randomDisplayTick(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final Random random) {
        this.modelBlock.randomDisplayTick(serverWorld, xCoord, yCoord, zCoord, random);
    }

    @Override
    public void onBlockClicked(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final EntityPlayer entityPlayer) {
        this.modelBlock.onBlockClicked(serverWorld, xCoord, yCoord, zCoord, entityPlayer);
    }

    @Override
    public void onBlockDestroyedByPlayer(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        this.modelBlock.onBlockDestroyedByPlayer(serverWorld, xCoord, yCoord, zCoord, nya4);
    }

    @Override
    public float getBlockBrightness(final IBlockAccess blockAccess, final int xCoord, final int yCoord, final int zCoord) {
        return this.modelBlock.getBlockBrightness(blockAccess, xCoord, yCoord, zCoord);
    }

    @Override
    public float getExplosionResistance(final Entity entity) {
        return this.modelBlock.getExplosionResistance(entity);
    }

    @Override
    public int getRenderBlockPass() {
        return this.modelBlock.getRenderBlockPass();
    }

    @Override
    public int idDropped(final int blockid, final Random random) {
        return this.modelBlock.idDropped(blockid, random);
    }

    @Override
    public int quantityDropped(final Random random) {
        return this.modelBlock.quantityDropped(random);
    }

    @Override
    public int getBlockTextureFromSideAndMetadata(final int textureIndexSlot, final int metadataValue) {
        return this.modelBlock.getBlockTextureFromSideAndMetadata(textureIndexSlot, metadataValue);
    }

    @Override
    public int getBlockTextureFromSide(final int textureIndexSlot) {
        return this.modelBlock.getBlockTextureFromSide(textureIndexSlot);
    }

    @Override
    public int getBlockTextureGeneric(final IBlockAccess blockAccess, final int xCoord, final int yCoord, final int zCoord, final int metadataValue) {
        return this.modelBlock.getBlockTextureGeneric(blockAccess, xCoord, yCoord, zCoord, metadataValue);
    }

    @Override
    public int tickRate() {
        return this.modelBlock.tickRate();
    }

    @Override
    public AABB getSelectedBoundingBoxFromPool(World world, final int xCoord, final int yCoord, final int zCoord) {
        return this.modelBlock.getSelectedBoundingBoxFromPool(world, xCoord, yCoord, zCoord);
    }

    @Override
    public void velocityToAddToEntity(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final Entity entity, final Vec3 var1) {
        this.modelBlock.velocityToAddToEntity(serverWorld, xCoord, yCoord, zCoord, entity, var1);
    }

    @Override
    public boolean isCollidable() {
        return this.modelBlock.isCollidable();
    }

    @Override
    public boolean canCollideCheck(final int nya1, final boolean boolean2) {
        return this.modelBlock.canCollideCheck(nya1, boolean2);
    }

    @Override
    public boolean canPlaceBlockAt(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        return this.modelBlock.canPlaceBlockAt(serverWorld, xCoord, yCoord, zCoord);
    }

    @Override
    public void onBlockAdded(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        this.onNeighborBlockChange(serverWorld, xCoord, yCoord, zCoord, 0);
        this.modelBlock.onBlockAdded(serverWorld, xCoord, yCoord, zCoord);
    }

    @Override
    public void onBlockRemoval(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        this.modelBlock.onBlockRemoval(serverWorld, xCoord, yCoord, zCoord);
    }

    @Override
    public void dropBlockAsItemWithChance(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4, final float nya5) {
        this.modelBlock.dropBlockAsItemWithChance(serverWorld, xCoord, yCoord, zCoord, nya4, nya5);
    }

    @Override
    public void dropBlockAsItem(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        this.modelBlock.dropBlockAsItem(serverWorld, xCoord, yCoord, zCoord, nya4);
    }

    @Override
    public void onEntityWalking(final World world, final int xCoord, final int yCoord, final int zCoord, final Entity entity) {
        this.modelBlock.onEntityWalking(world, xCoord, yCoord, zCoord, entity);
    }

    @Override
    public void updateTick(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final Random random) {
        this.modelBlock.updateTick(serverWorld, xCoord, yCoord, zCoord, random);
    }

    @Override
    public boolean blockActivated(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final EntityPlayer entityPlayer) {
        return this.modelBlock.blockActivated(serverWorld, xCoord, yCoord, zCoord, entityPlayer);
    }

    @Override
    public void onBlockDestroyedByExplosion(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        this.modelBlock.onBlockDestroyedByExplosion(serverWorld, xCoord, yCoord, zCoord);
    }
}
