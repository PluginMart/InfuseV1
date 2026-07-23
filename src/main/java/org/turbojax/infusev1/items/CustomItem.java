package org.turbojax.infusev1.items;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;

public abstract class CustomItem {
    protected final Infuse plugin;
    protected final String key;
    protected final NamespacedKey nsKey;

    public CustomItem(String key) {
        this.plugin = Infuse.getInstance();
        this.key = key;
        this.nsKey = new NamespacedKey(plugin, key);
    }

    public abstract ItemStack createItem();

    public CraftingRecipe createRecipe() {
        return MainConfig.createRecipe(key, createItem());
    }

    public NamespacedKey getKey() {
        return nsKey;
    }
}
