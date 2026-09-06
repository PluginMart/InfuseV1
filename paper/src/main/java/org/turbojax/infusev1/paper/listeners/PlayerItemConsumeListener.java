package org.turbojax.infusev1.paper.listeners;

import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.turbojax.infusev1.items.CustomItem;

public class PlayerItemConsumeListener implements Listener {
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = CraftItemStack.asNMSCopy(event.getItem());

        CustomItem ci = CustomItem.fromItemStack(item);
        if (ci == null) return;

        ItemStack newItem = ci.onConsume(((CraftPlayer) event.getPlayer()).getHandle(), item);

        event.setReplacement(CraftItemStack.asBukkitCopy(newItem));
    }
}
