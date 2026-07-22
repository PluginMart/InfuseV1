package org.turbojax.infusev1.items;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.MainConfig;

public class InfuseEffect extends CustomItem {
    public InfuseEffect() {
        super("infuse_effect");
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.POTION);

        item.editMeta(PotionMeta.class, meta -> {
            meta.setColor(Color.fromRGB(0xD2B48C));
            meta.setEnchantmentGlintOverride(true);
            meta.getPersistentDataContainer().set(nsKey, PersistentDataType.BOOLEAN, true);
        });

        return item;
    }

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (!item.getPersistentDataContainer().has(nsKey)) return;

        Player p = event.getPlayer();
        int pScore = DataManager.getScore(p);

        // Removing a random negative effect from the player if they have any.
        if (pScore < 0) {
            DataManager.setScore(p, pScore + 1);
            DataManager.removeRandomEffect(p);
            return;
        }

        // Making sure the player doesn't have the max number of positive effects
        // maybe replace with a "score" attribute that is the number of effects the player has
        if (pScore >= MainConfig.maxPositive()) {
            event.setCancelled(true);

            p.sendMessage("You already have the maximum number of positive effects");
            return;
        }

        // Adding a random positive effect
        DataManager.setScore(p, pScore + 1);
        DataManager.addRandomEffect(p, true);
    }
}
