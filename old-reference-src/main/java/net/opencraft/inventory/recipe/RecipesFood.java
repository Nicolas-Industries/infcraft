
package net.opencraft.client.inventory.recipe;

import net.opencraft.core.blocks.Block;
import net.opencraft.client.item.Item;
import net.opencraft.client.item.ItemStack;

public class RecipesFood {

    public void addRecipes(final CraftingManager gy) {
        gy.addRecipe(new ItemStack(Item.bowlSoup), "Y", "X", "#", 'X', Block.mushroomBrown, 'Y', Block.mushroomRed, '#', Item.bowlEmpty);
        gy.addRecipe(new ItemStack(Item.bowlSoup), "Y", "X", "#", 'X', Block.mushroomRed, 'Y', Block.mushroomBrown, '#', Item.bowlEmpty);
    }
}
