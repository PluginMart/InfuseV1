package org.turbojax.infusev1.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.inventories.ReviverMenu;

import java.util.List;

public class Reviver extends CustomItem implements Listener {
    public Reviver() {
        super("reviver");
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);

        item.editMeta(meta -> {
            meta.customName(Component.text("Revive Star", NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, State.FALSE));
            meta.lore(List.of(Component.text("Use to revive a player.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, State.FALSE)));
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

        // Skipping if no players have been banned
        if (DataManager.getBanned().size() == 0) {
            event.getPlayer().sendMessage(Component.text("No players have been banned yet."));
            return;
        }

        item.subtract();

        // Opening the reviver inventory
        event.getPlayer().openInventory(new ReviverMenu().getInventory());
    }
}