package org.turbojax.infusev1.inventories;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

public class ReviverMenu implements InventoryHolder {
    private final Inventory inventory;

    public ReviverMenu() {
        inventory = Bukkit.createInventory(this, 27, Component.text("Reviver"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}