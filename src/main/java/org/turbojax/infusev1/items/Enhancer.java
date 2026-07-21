package org.turbojax.infusev1.items;

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

import java.util.List;

public class Enhancer extends CustomItem implements Listener {
    public Enhancer() {
        super("enhancer");
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.POTION);
        
        item.editMeta(PotionMeta.class, meta -> {
            meta.setColor(Color.fromRGB(0x440044));
            meta.getPersistentDataContainer().set(nsKey, PersistentDataType.BOOLEAN, true);
        });

        return item;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (!item.getPersistentDataContainer().has(nsKey)) return;

        Player p = event.getPlayer();

        int duration = MainConfig.enhancerDuration() * 20;

        // TODO: Get all the player's effects        
        List.of(PotionEffectType.ABSORPTION).stream()
            .map(e -> new PotionEffect(e, duration, MainConfig.getEffectiveEnhancedLevel(e) - 1))
            .forEach(p::addPotionEffect);
    }
}
