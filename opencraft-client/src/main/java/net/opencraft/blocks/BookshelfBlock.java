
package net.opencraft.blocks;

import net.opencraft.blocks.material.Material;

import java.util.Random;

public class BookshelfBlock extends Block {

    public BookshelfBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture, Material.WOOD);
    }

    @Override
    public int getBlockTextureFromSide(final int textureIndexSlot) {
        if (textureIndexSlot <= 1) {
            return 4;
        }
        return this.blockIndexInTexture;
    }

    @Override
    public int quantityDropped(final Random random) {
        return 0;
    }
}
