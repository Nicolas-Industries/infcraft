
package net.infcraft.core.inventory.recipe;

import net.infcraft.core.blocks.Block;
import net.infcraft.core.item.ItemStack;

public class RecipesUtilities {

    public void addRecipes(final CraftingManager gy) {
        gy.addRecipe(new ItemStack(Block.chest), "###", "# #", "###", '#', Block.planks);
        gy.addRecipe(new ItemStack(Block.stoneOvenIdle), "###", "# #", "###", '#', Block.cobblestone);
        gy.addRecipe(new ItemStack(Block.workbench), "##", "##", '#', Block.planks);
    }
}
