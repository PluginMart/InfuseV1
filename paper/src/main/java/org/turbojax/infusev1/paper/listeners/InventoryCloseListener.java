package org.turbojax.infusev1.paper.listeners;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.turbojax.infusev1.inventories.ConfirmReviveMenu;
import org.turbojax.infusev1.inventories.ReviverMenu;

public class InventoryCloseListener implements Listener {
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player p)) return;

        ServerPlayer player = ((CraftPlayer) p).getHandle();
        AbstractContainerMenu containerMenu = player.containerMenu;

        if (containerMenu instanceof ReviverMenu m) {
            m.onClose(player);
        }

        if (containerMenu instanceof ConfirmReviveMenu m) {
            m.onClose(player);
        }
    }
}
