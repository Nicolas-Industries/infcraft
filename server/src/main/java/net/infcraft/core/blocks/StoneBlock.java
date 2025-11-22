
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;

import java.util.Random;

public class StoneBlock extends Block {

    public StoneBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture, Material.ROCK);
    }

    @Override
    public int idDropped(final int blockid, final Random random) {
        return Block.cobblestone.blockID;
    }
}
