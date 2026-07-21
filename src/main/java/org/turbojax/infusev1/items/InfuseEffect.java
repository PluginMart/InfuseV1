package org.turbojax.infusev1.items;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.turbojax.infusev1.MainConfig;

public class InfuseEffect extends CustomItem implements Listener {
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

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (!item.getPersistentDataContainer().has(nsKey)) return;

        Player p = event.getPlayer();

        // TODO: Check if the player has any negative effects

        List<PotionEffectType> activeEffects = List.of();

        // Making sure the player doesn't have the max number of effects
        // maybe replace with a "score" attribute that is the number of effects the player has
        if (activeEffects.size() == MainConfig.maxPositive()) {
            event.setCancelled(true);

            p.sendMessage("You already have the maximum number of positive effects");
            return;
        }


        // TODO: Get valid effects
        List<PotionEffectType> possibleEffects = MainConfig.positiveEffects();
        possibleEffects.removeAll(activeEffects);

        PotionEffectType effectType = possibleEffects.get((int) (Math.random() * possibleEffects.size()));
        PotionEffect effect = new PotionEffect(effectType, -1, MainConfig.getEffectiveLevel(effectType) - 1);
        p.addPotionEffect(effect);

        // TODO: Update playerdata
    }
}
