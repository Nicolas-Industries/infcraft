
package net.infcraft.core.world.gen;

import net.infcraft.core.blocks.Block;
import net.infcraft.core.blocks.FlowerBlock;
import net.infcraft.server.world.ServerWorld;

import java.util.Random;

public class WorldGenFlowers extends WorldGenerator {

    private int plantBlockId;

    public WorldGenFlowers(final int integer) {
        this.plantBlockId = integer;
    }

    @Override
    public boolean generate(final ServerWorld fe, final Random random, final int integer3, final int integer4, final int integer5) {
        for (int i = 0; i < 64; ++i) {
            final int xCoord = integer3 + random.nextInt(8) - random.nextInt(8);
            final int yCoord = integer4 + random.nextInt(4) - random.nextInt(4);
            final int zCoord = integer5 + random.nextInt(8) - random.nextInt(8);
            if (fe.getBlockId(xCoord, yCoord, zCoord) == 0 && ((FlowerBlock) Block.blocksList[this.plantBlockId]).canBlockStay(fe, xCoord, yCoord, zCoord)) {
                fe.setBlock(xCoord, yCoord, zCoord, this.plantBlockId);
            }
        }
        return true;
    }
}
