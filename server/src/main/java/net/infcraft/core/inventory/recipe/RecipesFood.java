
package net.infcraft.core.inventory.recipe;

import net.infcraft.core.blocks.Block;
import net.infcraft.core.item.Item;
import net.infcraft.core.item.ItemStack;

public class RecipesFood {

    public void addRecipes(final CraftingManager gy) {
        gy.addRecipe(new ItemStack(Item.bowlSoup), "Y", "X", "#", 'X', Block.mushroomBrown, 'Y', Block.mushroomRed, '#', Item.bowlEmpty);
        gy.addRecipe(new ItemStack(Item.bowlSoup), "Y", "X", "#", 'X', Block.mushroomRed, 'Y', Block.mushroomBrown, '#', Item.bowlEmpty);
    }
}
