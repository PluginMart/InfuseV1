package org.turbojax.infusev1.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.MainConfig;

public class PlayerRespawnListener implements Listener {
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        
        // Giving the player their effects when they respawn
        DataManager.getEffects(p)
            .stream()
            .map(e -> new PotionEffect(e, -1, MainConfig.getEffectiveLevel(e)))
            .forEach(p::addPotionEffect);
    }
}