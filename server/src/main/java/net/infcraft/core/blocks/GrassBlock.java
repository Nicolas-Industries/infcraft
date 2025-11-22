
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;
import net.infcraft.server.world.ServerWorld;

import java.util.Random;

public class GrassBlock extends Block {

    protected GrassBlock(final int blockid) {
        super(blockid, Material.GROUND);
        this.blockIndexInTexture = 3;
        this.setTickOnLoad(true);
    }

    @Override
    public int getBlockTextureFromSide(final int textureIndexSlot) {
        if (textureIndexSlot == 1) {
            return 0;
        }
        if (textureIndexSlot == 0) {
            return 2;
        }
        return 3;
    }

    @Override
    public void updateTick(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final Random random) {
        if (serverWorld.getBlockLightValue(xCoord, yCoord + 1, zCoord) < 4 && serverWorld.getBlockMaterial(xCoord, yCoord + 1, zCoord).isBlockGrass()) {
            if (random.nextInt(4) != 0) {
                return;
            }
            serverWorld.setBlockWithNotify(xCoord, yCoord, zCoord, Block.dirt.blockID);
        } else if (serverWorld.getBlockLightValue(xCoord, yCoord + 1, zCoord) >= 9) {
            final int n = xCoord + random.nextInt(3) - 1;
            final int n2 = yCoord + random.nextInt(5) - 3;
            final int n3 = zCoord + random.nextInt(3) - 1;
            if (serverWorld.getBlockId(n, n2, n3) == Block.dirt.blockID && serverWorld.getBlockLightValue(n, n2 + 1, n3) >= 4 && !serverWorld.getBlockMaterial(n, n2 + 1, n3).isBlockGrass()) {
                serverWorld.setBlockWithNotify(n, n2, n3, Block.grass.blockID);
            }
        }
    }

    @Override
    public int idDropped(final int blockid, final Random random) {
        return Block.dirt.idDropped(0, random);
    }
}
