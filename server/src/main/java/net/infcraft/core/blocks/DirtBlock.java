
package net.infcraft.core.blocks;

import net.infcraft.core.blocks.material.Material;

public class DirtBlock extends Block {

    protected DirtBlock(final int blockid, final int blockIndexInTexture) {
        super(blockid, blockIndexInTexture, Material.GROUND);
    }
}
