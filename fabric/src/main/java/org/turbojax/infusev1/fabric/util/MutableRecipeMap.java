package org.turbojax.infusev1.fabric.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface MutableRecipeMap {
    void addRecipe(RecipeHolder<?> recipe);
    boolean removeRecipe(ResourceKey<Recipe<?>> key);
}
