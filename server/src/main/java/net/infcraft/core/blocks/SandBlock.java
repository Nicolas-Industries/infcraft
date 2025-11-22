
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.core.entity.EntityFallingSand;
import net.infcraft.server.world.ServerWorld;

import java.util.Random;

public class SandBlock extends Block {

    public static boolean fallInstantly;

    static {
        SandBlock.fallInstantly = false;
    }

    public SandBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture, Material.SAND);
    }

    @Override
    public void onBlockAdded(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        serverWorld.scheduleBlockUpdate(xCoord, yCoord, zCoord, this.blockID);
    }

    @Override
    public void onNeighborBlockChange(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final int nya4) {
        serverWorld.scheduleBlockUpdate(xCoord, yCoord, zCoord, this.blockID);
    }

    @Override
    public void updateTick(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final Random random) {
        this.tryToFall(serverWorld, xCoord, yCoord, zCoord);
    }

    private void tryToFall(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        if (canFallBelow(serverWorld, xCoord, yCoord - 1, zCoord) && yCoord >= 0) {
            final EntityFallingSand entity = new EntityFallingSand(serverWorld, xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, this.blockID);
            if (SandBlock.fallInstantly) {
                while (!entity.isDead) {
                    entity.onUpdate();
                }
            } else {
                serverWorld.entityJoinedWorld(entity);
            }
        }
    }

    @Override
    public int tickRate() {
        return 3;
    }

    public static boolean canFallBelow(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord) {
        final int blockId = serverWorld.getBlockId(xCoord, yCoord, zCoord);
        if (blockId == 0) {
            return true;
        }
        if (blockId == Block.fire.blockID) {
            return true;
        }
        final Material blockMaterial = Block.blocksList[blockId].blockMaterial;
        return blockMaterial == Material.WATER || blockMaterial == Material.LAVA;
    }

}
