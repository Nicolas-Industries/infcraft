
package net.opencraft.core.inventory.recipe;

import net.opencraft.core.blocks.Block;
import net.opencraft.core.inventory.recipe.CraftingManager;
import net.opencraft.core.item.Item;
import net.opencraft.core.item.ItemStack;

public class RecipesFood {

    public void addRecipes(final CraftingManager gy) {
        gy.addRecipe(new ItemStack(Item.bowlSoup), "Y", "X", "#", 'X', Block.mushroomBrown, 'Y', Block.mushroomRed, '#', Item.bowlEmpty);
        gy.addRecipe(new ItemStack(Item.bowlSoup), "Y", "X", "#", 'X', Block.mushroomRed, 'Y', Block.mushroomBrown, '#', Item.bowlEmpty);
    }
}
