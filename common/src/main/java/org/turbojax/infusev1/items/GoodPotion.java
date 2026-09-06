package org.turbojax.infusev1.items;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NullMarked;
import org.turbojax.infusev1.DataManager;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.MainConfig;

import java.util.List;
import java.util.Optional;

@NullMarked
public class GoodPotion extends CustomItem {
    @Override
    public String key() {
        return "good_potion";
    }

    @Override
    public Component itemName() {
        return Component.literal("The Good Potion");
    }

    @Override
    public Item itemType() {
        return Items.POTION;
    }

    @Override
    public ItemLore itemLore() {
        return new ItemLore(List.of(Component.literal("A magical blessing that will clear a bad effect.")));
    }

    @Override
    public ItemStackTemplate createTemplate() {
        DataComponentPatch.Builder patch = DataComponentPatch.builder();

        patch.set(DataComponents.CUSTOM_DATA, customData());
        patch.set(DataComponents.CUSTOM_NAME, itemName());
        patch.set(DataComponents.LORE, itemLore());
        patch.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.POTION_CONTENTS, true));
        patch.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(0x55FFFF), List.of(), Optional.empty()));
        patch.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return new ItemStackTemplate(itemType(), patch.build());
    }

    @Override
    public ItemStack onConsume(Player player, ItemStack item) {
        DataManager dataManager = Infuse.getInstance().dataManager();
        MainConfig config = Infuse.getInstance().config();

        int pScore = dataManager.getScore(player);

        // Removing a random negative effect from the player if they have any.
        if (pScore < 0) {
            dataManager.setScore(player, pScore + 1);
            dataManager.removeRandomEffect(player, true);

            item.shrink(1);

            return item;
        }

        // Making sure the player doesn't have the max number of positive effects
        // maybe replace with a "score" attribute that is the number of effects the player has
        if (pScore >= config.maxScore()) {
            player.sendSystemMessage(Component.literal("You already have the maximum number of positive effects"));
            return item;
        }

        // Adding a random positive effect
        dataManager.setScore(player, pScore + 1);
        dataManager.addRandomEffect(player, true);

        item.shrink(1);
        return item;
    }
}
