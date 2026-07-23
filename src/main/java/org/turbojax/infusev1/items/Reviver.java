package org.turbojax.infusev1.items;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.turbojax.infusev1.inventories.ReviverMenu;

public class Reviver extends CustomItem implements Listener {
    public Reviver() {
        super("reviver");
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);

        item.editMeta(meta -> {
            // TODO: Custom item name
            // TODO: Custom lore
            meta.setEnchantmentGlintOverride(true);
            meta.getPersistentDataContainer().set(nsKey, PersistentDataType.BOOLEAN, true);
        });

        return item;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        // Skipping items that aren't this one
        if (item == null) return;
        if (!item.getPersistentDataContainer().has(nsKey)) return;

        item.subtract();

        // Opening the reviver inventory
        event.getPlayer().openInventory(new ReviverMenu().getInventory());
    }
}