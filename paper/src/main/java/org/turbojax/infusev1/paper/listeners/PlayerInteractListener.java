package org.turbojax.infusev1.paper.listeners;

import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.turbojax.infusev1.items.CustomItem;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = CraftItemStack.asNMSCopy(event.getItem());

        CustomItem ci = CustomItem.fromItemStack(item);
        if (ci == null) return;

        ci.interact(((CraftPlayer) event.getPlayer()).getHandle(), item);
    }
}
