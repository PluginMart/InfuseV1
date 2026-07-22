package org.turbojax.infusev1.items;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.MainConfig;

public class Enhancer extends CustomItem {
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

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (!item.getPersistentDataContainer().has(nsKey)) return;

        Player p = event.getPlayer();

        int duration = MainConfig.enhancerDuration() * 20;

        // Enhancing the player's effects
        DataManager.getEffects(p).stream()
            .map(e -> new PotionEffect(e, duration, MainConfig.getEffectiveEnhancedLevel(e) - 1))
            .forEach(p::addPotionEffect);
    }
}
