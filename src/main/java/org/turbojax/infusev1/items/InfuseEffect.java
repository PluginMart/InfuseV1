package org.turbojax.infusev1.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.MainConfig;

import java.util.List;

public class InfuseEffect extends CustomItem implements Listener {
    public InfuseEffect() {
        super("infuse_effect");
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.POTION);

        item.editMeta(PotionMeta.class, meta -> {
            meta.customName(Component.text("Infuse Potion", NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, State.FALSE));
            meta.lore(List.of(Component.text("Drink to gain an effect.", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, State.FALSE)));
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
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
