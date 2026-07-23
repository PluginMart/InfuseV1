package org.turbojax.infusev1.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.MainConfig;

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

        if (DataManager.getScore(p) <= 0) {
            p.sendMessage(Component.text("You have no positive effects to enhance...", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        int duration = MainConfig.enhancerDuration() * 20;

        // Enhancing the player's effects
        DataManager.getEffects(p).stream()
            .map(e -> new PotionEffect(e, duration, MainConfig.getEffectiveEnhancedLevel(e) - 1))
            .forEach(p::addPotionEffect);
    }
}
