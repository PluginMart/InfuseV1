package org.turbojax.infusev1.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.turbojax.infusev1.items.Enhancer;
import org.turbojax.infusev1.items.InfuseEffect;
import org.turbojax.infusev1.items.Reviver;

import java.util.SortedMap;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @WrapOperation(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;", at = @At(value = "INVOKE", target = "Ljava/util/SortedMap;size()I"))
    private int infusev1_insertCustomRecipes(SortedMap<Identifier, Recipe<?>> recipes, Operation<Integer> original) {
        recipes.put(Identifier.fromNamespaceAndPath("infusev1", "enhancer"), new Enhancer().getRecipe());
        recipes.put(Identifier.fromNamespaceAndPath("infusev1", "infuse_effect"), new InfuseEffect().getRecipe());
        recipes.put(Identifier.fromNamespaceAndPath("infusev1", "reviver"), new Reviver().getRecipe());

        return original.call(recipes);
    }
}
