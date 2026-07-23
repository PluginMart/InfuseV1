package org.turbojax.infusev1.listeners;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.MainConfig;

public class PlayerRespawnListener implements Listener {
    @EventHandler
    public void onRespawn(PlayerPostRespawnEvent event) {
        Player p = event.getPlayer();
        
        // Giving the player their effects when they respawn
        DataManager.getEffects(p)
            .stream()
            .map(e -> new PotionEffect(e, -1, MainConfig.getEffectiveLevel(e) - 1))
            .forEach(p::addPotionEffect);
    }
}
