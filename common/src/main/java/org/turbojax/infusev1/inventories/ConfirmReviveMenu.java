package org.turbojax.infusev1.inventories;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NonNull;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.items.Reviver;

public class ConfirmReviveMenu extends ChestMenu {
    private final Infuse infuse = Infuse.getInstance();
    private final Container container;
    private final int page;
    private final GameProfile profile;

    private boolean shouldRefund = true;

    public ConfirmReviveMenu(int containerId, Inventory inventory, int page, GameProfile profile) {
        super(MenuType.GENERIC_9x1, containerId, inventory, new SimpleContainer(9), 1);

        this.container = getContainer();
        this.page = page;
        this.profile = profile;

        ItemStack confirm = new ItemStack(Items.STAINED_GLASS_PANE.green());
        confirm.set(DataComponents.CUSTOM_NAME, Component.literal("Confirm").withStyle(Style.EMPTY.withColor(TextColor.GREEN).withBold(true)));

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        head.set(DataComponents.CUSTOM_NAME, Component.literal(profile.name()).withStyle(Style.EMPTY.withColor(TextColor.YELLOW).withItalic(false)));

        ItemStack deny = new ItemStack(Items.STAINED_GLASS_PANE.red());
        deny.set(DataComponents.CUSTOM_NAME, Component.literal("Deny").withStyle(Style.EMPTY.withColor(TextColor.RED).withBold(true)));

        container.setItem(1, confirm);
        container.setItem(2, confirm);
        container.setItem(4, head);
        container.setItem(6, deny);
        container.setItem(7, deny);
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, @NonNull ContainerInput containerInput, @NonNull Player player) {
        // Ignoring all clicks other than regular left clicks
        if (containerInput != ContainerInput.PICKUP) return;

        ItemStack clicked = container.getItem(slotIndex);

        // Ignoring if nothing was clicked
        if (clicked.isEmpty()) return;

        // Handling what item was clicked
        if (clicked.is(Items.STAINED_GLASS_PANE.green())) {
            shouldRefund = false;

            // Reviving the player
            infuse.dataManager().unban(new NameAndId(profile));

            // Closing the menu
            if (player instanceof ServerPlayer p) {
                p.closeContainer();
            }
        } else if (clicked.is(Items.STAINED_GLASS_PANE.red())) {
            shouldRefund = false;

            // Going back to the ReviverMenu
            player.openMenu(new ReviverMenuProvider(page));
        }
    }

    public void onClose(ServerPlayer player) {
        if (!shouldRefund) return;

        player.addItem(new Reviver().createItem());
    }
}
