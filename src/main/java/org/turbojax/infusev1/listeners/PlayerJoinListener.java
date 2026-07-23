package org.turbojax.infusev1.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.items.Enhancer;
import org.turbojax.infusev1.items.InfuseEffect;
import org.turbojax.infusev1.items.Reviver;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!DataManager.needsReset(player)) return;

        DataManager.resetEffects(player);
    }


    public void giveRecipes(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.discoverRecipe(new Enhancer().getKey());
        player.discoverRecipe(new InfuseEffect().getKey());
        player.discoverRecipe(new Reviver().getKey());
    }
}
