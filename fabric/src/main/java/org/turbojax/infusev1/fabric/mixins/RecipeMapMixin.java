package org.turbojax.infusev1.fabric.mixins;

import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.turbojax.infusev1.fabric.util.MutableRecipeMap;

import java.util.Collection;
import java.util.Map;

@Mixin(RecipeMap.class)
public abstract class RecipeMapMixin implements MutableRecipeMap {
    @Shadow
    @Final
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    @Final
    private Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;

    @Override
    @Unique
    public void addRecipe(RecipeHolder<?> recipe) {
        Collection<RecipeHolder<?>> recipes = byType.get(recipe.value().getType());

        if (byKey.containsKey(recipe.id())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.id());
        } else {
            recipes.add(recipe);
            byKey.put(recipe.id(), recipe);
        }
    }

    @Override
    @Unique
    public boolean removeRecipe(ResourceKey<Recipe<?>> key) {
        RecipeHolder<?> removed = byKey.remove(key);
        if (removed == null) return false;
        byType.get(removed.value().getType()).remove(removed);
        return true;
    }
}
