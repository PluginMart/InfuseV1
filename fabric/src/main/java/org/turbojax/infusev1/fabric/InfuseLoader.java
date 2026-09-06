package org.turbojax.infusev1.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.fabric.util.MutableRecipeManager;
import org.turbojax.infusev1.fabric.util.ReloadableResources;
import org.turbojax.infusev1.items.CustomItem;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class InfuseLoader extends Infuse implements DedicatedServerModInitializer {
    public MinecraftServer server;

    @Override
    public void onInitializeServer() {}

    @Override
    public Path configFile() {
        return Path.of("config", "infuse.yml");
    }

    @Override
    public Path dataFile() {
        return Path.of("data", "infuse.yml");
    }

    @Override
    public void reloadRecipes() {
        var enabled = CustomItem.getRegisteredItems()
                .values()
                .stream()
                .map(item -> {
                    ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, item.id());

                    // Removing the recipe so it can be re-added
                    ((MutableRecipeManager) server.getRecipeManager()).infusev1$removeRecipe(recipeKey);

                    CraftingRecipe recipe = item.getRecipe();
                    if (recipe == null) return null;

                    ((MutableRecipeManager) server.getRecipeManager()).infusev1$addRecipe(new RecipeHolder<>(recipeKey, recipe));
                    return recipeKey;
                })
                .filter(Objects::nonNull)
                .toList();

        ((ReloadableResources) server.getPlayerList()).infusev1$reloadRecipes();

        // Reloading the recipes for the player.
        for (Player player : server.getPlayerList().getPlayers()) {
            List<RecipeHolder<?>> recipes = enabled.stream().flatMap((id) -> this.server.getRecipeManager().byKey(id).stream()).toList();
            player.resetRecipes(recipes);
            player.awardRecipes(recipes);
        }
    }
}
