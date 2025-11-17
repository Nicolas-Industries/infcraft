
package net.opencraft.core.item;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.item.ItemTool;

public class ItemAxe extends ItemTool {

    private static Block[] blocksEffectiveAgainst;

    public ItemAxe(final int itemid, final int toolTier) {
        super(itemid, 3, toolTier, ItemAxe.blocksEffectiveAgainst);
    }

    static {
        ItemAxe.blocksEffectiveAgainst = new Block[]{Block.planks, Block.bookshelf, Block.wood, Block.chest};
    }
}
