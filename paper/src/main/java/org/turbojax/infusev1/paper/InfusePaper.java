package org.turbojax.infusev1.paper;

import net.minecraft.world.item.crafting.CraftingRecipe;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.items.CustomItem;

import java.nio.file.Path;

public class InfusePaper extends Infuse {
    public InfusePaper() {
        config.load();
        dataManager.load();
    }

    @Override
    public Path configFile() {
        return Path.of("plugins", "Infuse", "config.yml");
    }

    @Override
    public Path dataFile() {
        return Path.of("plugins", "Infuse", "playerdata.yml");
    }

    @Override
    public void reloadRecipes() {
        CustomItem.getRegisteredItems().forEach((_, item) -> {
            CraftingRecipe recipe = item.getRecipe();
            if (recipe == null) return;

            Bukkit.addRecipe(recipe.toBukkitRecipe(CraftNamespacedKey.fromMinecraft(item.id())));
        });
    }
}