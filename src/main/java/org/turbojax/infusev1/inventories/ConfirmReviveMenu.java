package org.turbojax.infusev1.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.items.Reviver;

public class ConfirmReviveMenu implements InventoryHolder, Listener {
    private final Inventory inventory;
    private final int page;
    private final OfflinePlayer player;
    private boolean shouldRefund = true;

    public ConfirmReviveMenu(int page, OfflinePlayer player) {
        this.inventory = Bukkit.createInventory(this, 9, Component.text("Confirm Revive"));
        this.page = page;
        this.player = player;

        ItemStack confirm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        confirm.editMeta(m -> m.customName(Component.text("Confirm", NamedTextColor.GREEN, TextDecoration.BOLD)));

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, m -> m.setOwningPlayer(player));

        ItemStack deny = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        deny.editMeta(m -> m.customName(Component.text("Deny", NamedTextColor.RED, TextDecoration.BOLD)));

        inventory.setItem(1, confirm);
        inventory.setItem(2, confirm);
        inventory.setItem(4, head);
        inventory.setItem(6, deny);
        inventory.setItem(7, deny);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ConfirmReviveMenu menu)) return;
        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof ConfirmReviveMenu) {
            event.setCancelled(true);
        } else {
            event.setCancelled(event.getClick() == ClickType.SHIFT_LEFT);
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            // Unbanning the selected player
            DataManager.unban(menu.player);

            menu.shouldRefund = false;

            event.getView().close();
        } else if (item.getType() == Material.RED_STAINED_GLASS_PANE) {
            menu.shouldRefund = false;

            // Going back to the ReviverMenu
            event.getViewers().getFirst().openInventory(new ReviverMenu(menu.page).getInventory());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof ConfirmReviveMenu menu)) return;
        if (!menu.shouldRefund) return;

        event.getPlayer().getInventory().addItem(new Reviver().createItem());
    }
}
