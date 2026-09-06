package org.turbojax.infusev1.fabric.mixins;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
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
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.turbojax.infusev1.fabric.util.MutableRecipeMap;

import java.util.Collection;
import java.util.Map;

@Mixin(RecipeMap.class)
public abstract class RecipeMapMixin implements MutableRecipeMap {
    @Invoker("<init>")
    private static RecipeMap createInstance(final Multimap<RecipeType<?>, RecipeHolder<?>> byType, final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey) {
        throw new IllegalStateException("This method should never be called");
    }

    @Shadow
    @Final
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    @Final
    private Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;

    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void infusev1$onCreate(Iterable<RecipeHolder<?>> recipes, CallbackInfoReturnable<RecipeMap> cir) {
        MutableRecipeMap old = (MutableRecipeMap) cir.getReturnValue();
        RecipeMap newMap = createInstance(LinkedHashMultimap.create(old.byType()), Maps.newLinkedHashMap(old.byKey()));
        cir.setReturnValue(newMap);
    }

    @Override
    @Unique
    public void infusev1$addRecipe(RecipeHolder<?> recipe) {
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
    public boolean infusev1$removeRecipe(ResourceKey<Recipe<?>> key) {
        RecipeHolder<?> removed = byKey.remove(key);
        if (removed == null) return false;
        byType.get(removed.value().getType()).remove(removed);
        return true;
    }

    @Override
    public Multimap<RecipeType<?>, RecipeHolder<?>> byType() {
        return byType;
    }

    @Override
    public Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey() {
        return byKey;
    }
}
