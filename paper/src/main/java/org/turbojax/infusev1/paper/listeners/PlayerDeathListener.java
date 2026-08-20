package org.turbojax.infusev1.paper.listeners;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.turbojax.infusev1.Infuse;

public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Infuse.getInstance().onDeath(((CraftPlayer) event.getPlayer()).getHandle());
    }
}
