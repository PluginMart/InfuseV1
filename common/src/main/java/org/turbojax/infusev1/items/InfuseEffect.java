package org.turbojax.infusev1.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.Infuse;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NullMarked
public class InfuseEffect extends CustomItem {
    public static final InfuseEffect EMPTY = new InfuseEffect(MobEffects.ABSORPTION, "");
    public static final String EFFECT_KEY = "infusev1:stored_effect";

    private final Holder<MobEffect> effect;
    private final String owner;

    public InfuseEffect(Holder<MobEffect> effect, String owner) {
        this.effect = effect;
        this.owner = owner;
    }

    @Override
    public String key() {
        return "infuse_effect";
    }

    @Override
    public Component itemName() {
        String effectName = effect.unwrapKey().orElseThrow().identifier().toShortString();

        effectName = Arrays.stream(effectName.split("_"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(" "));

        return Component.literal(effectName);
    }

    @Override
    public Item itemType() {
        return Items.POTION;
    }

    @Override
    public ItemLore itemLore() {
        return new ItemLore(List.of(Component.literal("Extracted from " + owner)));
    }

    @Override
    public CustomData customData() {
        CustomData old = super.customData();

        CompoundTag tag = old.copyTag();
        tag.putString(EFFECT_KEY, effect.unwrapKey().get().identifier().toString());

        return CustomData.of(tag);
    }

    @Override
    public ItemStackTemplate createTemplate() {
        DataComponentPatch.Builder patch = DataComponentPatch.builder();

        patch.set(DataComponents.CUSTOM_DATA, customData());
        patch.set(DataComponents.CUSTOM_NAME, itemName());
        patch.set(DataComponents.LORE, itemLore());
        patch.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.POTION_CONTENTS, true));
        patch.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(effect.value().getColor()), List.of(), Optional.empty()));
        patch.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return new ItemStackTemplate(itemType(), patch.build());
    }

    public Holder.@Nullable Reference<MobEffect> getEffect(ItemStack item) {
        if (!isItem(item)) return null;

        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        CompoundTag tag = data.copyTag();
        Optional<String> effectKey = tag.getString(EFFECT_KEY);
        if (effectKey.isEmpty()) return null;

        Identifier effectId = Identifier.parse(effectKey.get());
        var effect = BuiltInRegistries.MOB_EFFECT.get(effectId);

        return effect.orElse(null);
    }

    @Override
    public ItemStack onConsume(Player player, ItemStack item) {
        DataManager dataManager = Infuse.getInstance().dataManager();
        int pScore = dataManager.getScore(player);

        // Removing a random negative effect from the player if they have any.
        if (pScore < 0) {
            dataManager.setScore(player, pScore + 1);
            dataManager.removeRandomEffect(player, true);

            item.shrink(1);

            return item;
        }

        // Getting the effect to give to the player
        Holder.Reference<MobEffect> effect = getEffect(item);
        if (effect == null) {
            Infuse.LOGGER.error("Failed to get an effect from an Infuse Effect!");
            Infuse.LOGGER.error("Holder: {}", player.getPlainTextName());

            player.sendSystemMessage(Component.literal("Something went wrong while parsing the effect.  Contact an administrator").withColor(ChatFormatting.RED.getColor()));
            return item;
        }

        // If the player already has the effect, don't let them drink it.
        if (dataManager.hasEffect(player, effect)) {
            player.sendSystemMessage(Component.literal("You already have this effect!").withColor(ChatFormatting.RED.getColor()));
            return item;
        }

        // Increasing the player's score
        dataManager.setScore(player, pScore + 1);

        // Giving the player the effect
        dataManager.addEffect(player, effect);

        player.sendSystemMessage(Component.literal("You recieved ").withColor(ChatFormatting.GREEN.getColor()).append(Infuse.getEffectName(effect)));

        item.shrink(1);
        return item;
    }
}
