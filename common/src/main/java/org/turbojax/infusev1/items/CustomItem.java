package org.turbojax.infusev1.items;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;

public interface CustomItem {
    String key();

    Component itemName();

    Item itemType();

    ItemLore itemLore();

    default boolean isItem(ItemStack item) {
        if (!item.is(itemType())) return false;

        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;

        Optional<String> key = data.copyTag().getString("infuse:item_key");
        return key.map(key().toString()::equals).orElse(false);
    }

    default DataComponentPatch.Builder startPatches() {
        CompoundTag tag = new CompoundTag();
        tag.putString("infuse:item_key", key());

        return DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_NAME, itemName())
                .set(DataComponents.LORE, itemLore())
                .set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    ItemStackTemplate createTemplate();

    default ItemStack createItem() {
        return createTemplate().withCount(1).create();
    }

    default CraftingRecipe getRecipe() {
        MainConfig config = Infuse.getInstance().config();

        return config.getRecipe(this);
    }

    default JsonElement asJson() {
        return ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, createItem()).getOrThrow();
    }

    /**
     * Defines what should happen when a player interacts with the item.
     *
     * @param player The {@link Player} who interacted with the item.
     * @param item The {@link ItemStack} the player interacted with.
     */
    @Nullable
    default InteractionResult interact(Player player, ItemStack item) {
        return null;
    }

    /**
     * Defines what should happen when a player consumes the item.
     *
     * @param player The {@link Player} who consumed the item.
     * @param item The {@link ItemStack} the player consumed.
     */
    default ItemStack consume(Player player, ItemStack item) {
        return item;
    }

    @Nullable
    static CustomItem fromKey(String key) {
        if (key.equalsIgnoreCase("enhancer")) return new Enhancer();
        if (key.equalsIgnoreCase("infuse_effect")) return new InfuseEffect();
        if (key.equalsIgnoreCase("reviver")) return new Reviver();

        return null;
    }

    static boolean isCustomItem(ItemStack item) {
        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;

        return data.copyTag().contains("infuse:item_key");
    }

    @Nullable
    static String getKey(ItemStack item) {
        if (!isCustomItem(item)) return null;

        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        Optional<String> key = data.copyTag().getString("infuse:item_key");

        return key.orElse(null);
    }

    @Nullable
    static CustomItem fromItemStack(ItemStack item) {
        String key = getKey(item);
        if (key == null) return null;

        return fromKey(key);
    }
}
