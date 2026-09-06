package org.turbojax.infusev1.fabric.mixins;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.turbojax.infusev1.fabric.util.MutableRecipeManager;
import org.turbojax.infusev1.fabric.util.MutableRecipeMap;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements MutableRecipeManager {
    @Shadow
    private RecipeMap recipes;

    @Unique
    FeatureFlagSet enabledFlags;

    @Shadow
    public abstract void finalizeRecipeLoading(FeatureFlagSet enabledFlags);

    @Override
    @Unique
    public void infusev1$addRecipe(RecipeHolder<?> recipe) {
        ((MutableRecipeMap)recipes).infusev1$addRecipe(recipe);
        finalizeRecipeLoading(enabledFlags);
    }

    @Override
    @Unique
    public boolean infusev1$removeRecipe(ResourceKey<Recipe<?>> key) {
        boolean removed = ((MutableRecipeMap)recipes).infusev1$removeRecipe(key);
        if (removed) {
            this.finalizeRecipeLoading();
        }
        return removed;
    }

    @Unique
    public void finalizeRecipeLoading() {
        this.finalizeRecipeLoading(this.enabledFlags);
    }

    @Inject(method="finalizeRecipeLoading",at=@At("HEAD"))
    public void onRecipeLoad(FeatureFlagSet enabledFlags, CallbackInfo ci) {
        this.enabledFlags = enabledFlags;
    }
}
