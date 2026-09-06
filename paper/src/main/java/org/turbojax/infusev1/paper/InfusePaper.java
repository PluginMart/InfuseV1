package org.turbojax.infusev1.paper;

import net.minecraft.world.item.crafting.CraftingRecipe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.items.CustomItem;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class InfusePaper extends Infuse {
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
        List<NamespacedKey> enabled = CustomItem.getRegisteredItems()
            .values()
            .stream()
            .map(item -> {
                CraftingRecipe recipe = item.getRecipe();
                if (recipe == null) return null;

                var recipeId = CraftNamespacedKey.fromMinecraft(item.id());
                Bukkit.removeRecipe(recipeId);
                Bukkit.addRecipe(recipe.toBukkitRecipe(recipeId));
                return recipeId;
            })
            .filter(Objects::nonNull)
            .toList();

        for (Player player : Bukkit.getOnlinePlayers()) {
            enabled.forEach(player::discoverRecipe);
        }
    }
}