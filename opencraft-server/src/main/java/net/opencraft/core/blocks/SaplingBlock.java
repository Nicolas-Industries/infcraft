
package net.opencraft.core.blocks;

import net.opencraft.server.world.ServerWorld;
import net.opencraft.core.world.gen.WorldGenTrees;

import java.util.Random;

public class SaplingBlock extends FlowerBlock {

    protected SaplingBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture);
        final float n = 0.4f;
        this.setShape(0.5f - n, 0.0f, 0.5f - n, 0.5f + n, n * 2.0f, 0.5f + n);
    }

    @Override
    public void updateTick(final ServerWorld serverWorld, final int xCoord, final int yCoord, final int zCoord, final Random random) {
        super.updateTick(serverWorld, xCoord, yCoord, zCoord, random);
        if (serverWorld.getBlockLightValue(xCoord, yCoord + 1, zCoord) >= 9 && random.nextInt(5) == 0) {
            final int blockMetadata = serverWorld.getBlockMetadata(xCoord, yCoord, zCoord);
            if (blockMetadata < 15) {
                serverWorld.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, blockMetadata + 1);
            } else {
                serverWorld.setBlock(xCoord, yCoord, zCoord, 0);
                if (!new WorldGenTrees().generate(serverWorld, random, xCoord, yCoord, zCoord)) {
                    serverWorld.setBlock(xCoord, yCoord, zCoord, this.blockID);
                }
            }
        }
    }
}
