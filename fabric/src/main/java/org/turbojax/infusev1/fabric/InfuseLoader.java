package org.turbojax.infusev1.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.fabric.util.MutableRecipeManager;
import org.turbojax.infusev1.fabric.util.ReloadableResources;
import org.turbojax.infusev1.items.CustomItem;

import java.nio.file.Path;

public class InfuseLoader extends Infuse implements DedicatedServerModInitializer {
    public MinecraftServer server;

    @Override
    public void onInitializeServer() {
        // Loading the config/data
        config.load();
        dataManager.load();
    }

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
        CustomItem.getRegisteredItems().forEach((key, item) -> {
            CraftingRecipe recipe = item.getRecipe();
            if (recipe == null) return;

            ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, item.id());
            ((MutableRecipeManager) server.getRecipeManager()).infusev1$addRecipe(new RecipeHolder<>(recipeKey, recipe));
        });

        ((ReloadableResources) server.getPlayerList()).infusev1$reloadRecipes();
    }
}
