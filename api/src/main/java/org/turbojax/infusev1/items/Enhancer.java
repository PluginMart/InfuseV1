package org.turbojax.infusev1.items;

import java.util.List;
import java.util.Optional;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.InfuseProvider;

public class Enhancer implements CustomItem {
    private final Infuse infuse = InfuseProvider.get();

    @Override
    public String key() {
        return "enhancer";
    }

    @Override
    public Item itemType() {
        return Items.POTION;
    }

    @Override
    public Component itemName() {
        Style style = Style.EMPTY
                .withBold(true)
                .withItalic(false)
                .withColor(TextColor.DARK_PURPLE);

        return Component.literal("Infuse Enhancer").setStyle(style);
    }

    @Override
    public ItemLore itemLore() {
        Style style = Style.EMPTY
                .withItalic(false)
                .withColor(TextColor.WHITE);

        List<Component> lines = List.of(Component.literal("Makes your effects stronger for " + infuse.config().enhancerDuration() + "s.").setStyle(style));

        return new ItemLore(lines);
    }

    @Override
    public ItemStackTemplate createTemplate() {
        DataComponentPatch.Builder patch = startPatches();
        patch.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, ReferenceLinkedOpenHashSet.of(DataComponents.POTION_CONTENTS)));
        patch.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(0x440044), List.of(), Optional.empty()));

        return new ItemStackTemplate(itemType(), patch.build());
    }

    @Override
    public ItemStack consume(Player player, ItemStack item) {
        if (infuse.dataManager().getScore(player) <= 0) {
            player.sendSystemMessage(Component.literal("You have no positive effects to enhance...").withColor(TextColor.RED));
            return item;
        }

        int duration = infuse.config().enhancerDuration() * 20;

        // Enhancing the player's effects
        infuse.dataManager().getEffects(player).stream()
                .map(e -> new MobEffectInstance(e, duration, infuse.config().getEffectiveEnhancedAmplifier(e)))
                .forEach(player::addEffect);

        item.shrink(1);

        return item;
    }
}
