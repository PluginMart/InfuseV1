package org.turbojax.infusev1.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;

@NullMarked
public abstract class CustomItem {
    public static final String ITEM_KEY = "infusev1:item_key";
    private static final Map<String,CustomItem> REGISTERED = new HashMap<>();

    /**
     * Registers a custom item.
     * @param item The custom item to register.
     */
    public static void register(CustomItem item) {
        REGISTERED.put(item.key(), item);
    }

    /**
     * Register multiple custom items.
     * @param items The custom items to register.
     */
    public static void register(CustomItem... items) {
        for (CustomItem item : items) register(item);
    }

    /**
     * Unregisters a custom item.
     * @param item The custom item to unregister.
     */
    public static void unregister(CustomItem item) {
        REGISTERED.remove(item.key());
    }

    /**
     * Gets the currently registered items.
     * Changes to this map will not affect the registered items.
     */
    @Unmodifiable
    public static Map<String, CustomItem> getRegisteredItems() {
        return Map.copyOf(REGISTERED);
    }

    /**
     * Gets a custom item by its key.
     *
     * @param key The key of the custom item.
     * @return The custom item, or null if not found.
     */
    @Nullable
    public static CustomItem fromKey(String key) {
        return REGISTERED.get(key);
    }

    /**
     * Checks if an {@link ItemStack} is a custom item.
     *
     * @param item The {@link ItemStack} to check.
     * @return True if the item is a registered custom item, false otherwise.
     */
    public static boolean isCustomItem(ItemStack item) {
        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;

        Tag t = data.copyTag().remove(ITEM_KEY);
        if (t == null) return false;

        Optional<String> key = t.asString();
        return key.filter(REGISTERED::containsKey).isPresent();
    }

    /**
     * Parses a custom item's key from an {@link ItemStack}.
     *
     * @param item The {@link ItemStack} to parse.
     * @return The key of the custom item, or null if the item is not a custom item or does not have a key.
     */
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public static String getKey(ItemStack item) {
        if (!isCustomItem(item)) return null;

        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        Optional<String> key = data.copyTag().getString(ITEM_KEY);

        return key.orElse(null);
    }

    /**
     * Attempts to convert an {@link ItemStack} to a {@link CustomItem}.
     *
     * @param item The {@link ItemStack} to convert.
     * @return The {@link CustomItem} if the item represents a registered custom item, null otherwise.
     */
    @Nullable
    public static CustomItem fromItemStack(ItemStack item) {
        String key = getKey(item);
        if (key == null) return null;

        return fromKey(key);
    }

    public abstract String key();
    public abstract Component itemName();
    public abstract Item itemType();
    public abstract ItemLore itemLore();
    public abstract ItemStackTemplate createTemplate();

    /** Gets the id of the custom item. */
    public Identifier id() {
        return Infuse.id(key());
    }

    public CustomData customData() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ITEM_KEY, key());

        return CustomData.of(tag);
    }

    /**
     * Checks if the given {@link ItemStack} is a custom item of this type.
     *
     * @param item The {@link ItemStack} to check.
     * @return True if the item is a custom item of this type, false otherwise.
     */
    public boolean isItem(ItemStack item) {
        if (!item.is(itemType())) return false;

        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;

        Optional<String> key = data.copyTag().getString(ITEM_KEY);
        return key.map(key()::equals).orElse(false);
    }

    /**
     * Creates an {@link ItemStack} of this custom item.
     * @return The created {@link ItemStack}.
     */
    public ItemStack createItem() {
        return createItem(1);
    }

    /**
     * Creates an {@link ItemStack} of this custom item with the specified count.
     *
     * @param count The number if items in the stack.
     * @return The created {@link ItemStack}.
     */
    public ItemStack createItem(int count) {
        return createTemplate().withCount(count).create();
    }

    @Nullable
    public CraftingRecipe getRecipe() {
        MainConfig config = Infuse.getInstance().config();

        return config.getRecipe(this);
    }

    /**
     * Defines what should happen when a player consumes the item.
     * The default behavior is overridden.
     *
     * @param player The {@link Player} who consumed the item.
     * @param item The {@link ItemStack} the player consumed.
     * @return The {@link ItemStack} to be returned to the player.
     */
    public ItemStack onConsume(Player player, ItemStack item) {
        return ItemStack.EMPTY;
    }
}
