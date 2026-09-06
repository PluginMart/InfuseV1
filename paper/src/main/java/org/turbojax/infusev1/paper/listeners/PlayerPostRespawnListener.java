package org.turbojax.infusev1.paper.listeners;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.turbojax.infusev1.Infuse;

public class PlayerPostRespawnListener implements Listener {
    @EventHandler
    public void postRespawn(PlayerPostRespawnEvent event) {
        Infuse.getInstance().postRespawn(((CraftPlayer) event.getPlayer()).getHandle());
    }
}
