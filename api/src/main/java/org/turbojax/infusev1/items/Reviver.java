package org.turbojax.infusev1.items;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.turbojax.infusev1.InfuseProvider;
import org.turbojax.infusev1.inventories.ReviverMenuProvider;

import java.util.List;

public class Reviver implements CustomItem {

    @Override
    public String key() {
        return "reviver";
    }

    @Override
    public Component itemName() {
        Style style = Style.EMPTY
                .withColor(TextColor.YELLOW)
                .withBold(true)
                .withItalic(false);

        return Component.literal("Revive Star").setStyle(style);
    }

    @Override
    public Item itemType() {
        return Items.NETHER_STAR;
    }

    @Override
    public ItemLore itemLore() {
        Style style = Style.EMPTY
                .withColor(TextColor.WHITE)
                .withItalic(false);

        return new ItemLore(List.of(Component.literal("Use to revive a player.").setStyle(style)));
    }

    @Override
    public ItemStackTemplate createTemplate() {
        DataComponentPatch.Builder patch = startPatches();
        patch.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return new ItemStackTemplate(itemType(), patch.build());
    }

    @Override
    public InteractionResult interact(Player player, ItemStack item) {
        // Skipping if no players have been banned
        if (InfuseProvider.get().dataManager().getBanned().isEmpty()) {
            player.sendSystemMessage(Component.literal("No players have been banned yet."));
            return InteractionResult.FAIL;
        }

        player.openMenu(new ReviverMenuProvider(0));
        item.shrink(1);

        return InteractionResult.SUCCESS.heldItemTransformedTo(item);
    }
}
