package org.turbojax.infusev1.paper.listeners;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.turbojax.infusev1.Infuse;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Infuse.getInstance().onJoin(((CraftPlayer) event.getPlayer()).getHandle());
    }
}
