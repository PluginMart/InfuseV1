package org.turbojax.infusev1.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.MainConfig;

import java.util.Date;

public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player dead = event.getPlayer();
        int deadScore = DataManager.getScore(dead);
        Player killer = dead.getKiller();
        if (killer == null) return;
        int killerScore = DataManager.getScore(killer);

        if (deadScore - 1 >= MainConfig.minScore()) {
            // Updating the player's score
            DataManager.setScore(dead, deadScore - 1);

            // Banning the player if necessary
            int banScore = MainConfig.banScore();
            if (banScore < 0 && deadScore - 1 == banScore) {
                dead.ban("Ran out of lives!", (Date) null, null);
                DataManager.ban(dead);
            }

            if (deadScore > 0 && deadScore <= MainConfig.maxPositive()) {
                // Removing a random positive effect
                DataManager.removeRandomEffect(dead);
            } else if (deadScore <= 0 && deadScore > -MainConfig.maxNegative()) {
                // Giving a random negative effect
                DataManager.addRandomEffect(dead, false);
            }
        }

        if (killerScore + 1 <= MainConfig.maxScore()) {
            // Updating the player's score
            DataManager.setScore(killer, killerScore + 1);

            if (killerScore < 0 && killerScore >= -MainConfig.maxNegative()) {
                // Removing a random negative effect
                DataManager.removeRandomEffect(killer);
            } else if (killerScore >= 0 && killerScore < MainConfig.maxPositive()) {
                // Giving a random positive effect
                DataManager.addRandomEffect(killer, true);
            }
        }
    }
}
