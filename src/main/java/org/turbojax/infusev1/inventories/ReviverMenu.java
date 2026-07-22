package org.turbojax.infusev1.inventories;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.items.Reviver;

import java.util.List;

public class ReviverMenu implements InventoryHolder, Listener {
    private static final NamespacedKey DEST_KEY = new NamespacedKey("infuse", "dest");
    private static final int headsPerPage = 7;

    private final Inventory inventory;
    private final int page;
    private boolean shouldRefund = true;

    public ReviverMenu() {
        this(0);
    }

    public ReviverMenu(int page) {
        this.inventory = Bukkit.createInventory(this, 27, Component.text("Reviver"));
        this.page = page;

        List<OfflinePlayer> banned = DataManager.getBanned();
        
        int lastPage = (int) Math.ceil(banned.size() / headsPerPage);

        if (page < 0 || page > lastPage) throw new IllegalArgumentException("Invalid ReviverMenu page '" + page + "'");

        // Adding the arrows
        if (page != 0) inventory.setItem(21, createArrow(page - 1));
        if (page != lastPage) inventory.setItem(23, createArrow(page + 1));


        // Adding the heads
        for (int i = 0; i < headsPerPage; i++) {
            int index = i + headsPerPage * page;
            if (banned.size() <= index) return;

            inventory.setItem(i + 10, createHead(banned.get(index)));
        }
    }

    public ItemStack createHead(OfflinePlayer player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);

        skull.editMeta(SkullMeta.class, m -> {
            m.customName(Component.text(player.getName()));
            m.setOwningPlayer(player);
        });

        return skull;
    }

    public ItemStack createArrow(int dest) {
        ItemStack arrow = new ItemStack(Material.FEATHER);

        arrow.editMeta(m -> {
            m.customName(Component.text("To page " + dest));
            m.getPersistentDataContainer().set(DEST_KEY, PersistentDataType.INTEGER, dest);
        });

        return arrow;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Handles when this inventory is clicked
     * @param event
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ReviverMenu menu)) return;
        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof ReviverMenu) {
            event.setCancelled(true);
        } else {
            event.setCancelled(event.getClick() == ClickType.SHIFT_LEFT);
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item.getType() == Material.FEATHER) {
            // Parsing the page number
            int page = item.getPersistentDataContainer().get(DEST_KEY, PersistentDataType.INTEGER);

            menu.shouldRefund = false;

            // Opening the new page
            event.getViewers().getFirst().openInventory(new ReviverMenu(page).getInventory());
        } else if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            OfflinePlayer toRevive = meta.getOwningPlayer();

            menu.shouldRefund = false;

            // Opening the revive confirmation menu
            event.getViewers().getFirst().openInventory(new ConfirmReviveMenu(menu.page, toRevive).getInventory());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof ReviverMenu menu)) return;
        if (!menu.shouldRefund) return;

        event.getPlayer().getInventory().addItem(new Reviver().createItem());
    }
}