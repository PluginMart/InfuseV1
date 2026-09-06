package org.turbojax.infusev1.fabric.util;

import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Map;

public interface MutableRecipeMap {
    void infusev1$addRecipe(RecipeHolder<?> recipe);
    boolean infusev1$removeRecipe(ResourceKey<Recipe<?>> key);

    Multimap<RecipeType<?>, RecipeHolder<?>> byType();
    Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey();
}
