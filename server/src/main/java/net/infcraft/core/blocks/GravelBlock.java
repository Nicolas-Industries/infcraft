
package net.infcraft.core.blocks;

import net.infcraft.core.item.Item;

import java.util.Random;

public class GravelBlock extends SandBlock {

    public GravelBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture);
    }

    @Override
    public int idDropped(final int blockid, final Random random) {
        if (random.nextInt(10) == 0) {
            return Item.flint.shiftedIndex;
        }
        return this.blockID;
    }
}
