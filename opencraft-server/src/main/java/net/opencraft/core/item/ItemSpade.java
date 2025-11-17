
package net.opencraft.core.item;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.item.ItemTool;

public class ItemSpade extends ItemTool {

    private static Block[] blocksEffectiveAgainst;

    public ItemSpade(final int itemid, final int toolTier) {
        super(itemid, 1, toolTier, ItemSpade.blocksEffectiveAgainst);
    }

    static {
        ItemSpade.blocksEffectiveAgainst = new Block[]{Block.grass, Block.dirt, Block.sand, Block.gravel};
    }
}
