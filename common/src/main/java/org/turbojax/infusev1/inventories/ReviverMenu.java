package org.turbojax.infusev1.inventories;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NonNull;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.items.Reviver;

import java.util.List;

public class ReviverMenu extends ChestMenu {
    private static final Identifier DEST_KEY = Identifier.fromNamespaceAndPath("infusev1", "dest");
    private static final int headsPerPage = 7;

    private final Infuse infuse = Infuse.getInstance();
    private final Container container;
    private final int page;

    private boolean shouldRefund = true;

    public ReviverMenu(int containerId, Inventory inventory, int page) {
        super(MenuType.GENERIC_9x3, containerId, inventory, new SimpleContainer(27), 3);

        this.page = page;
        this.container = getContainer();

        List<NameAndId> banned = infuse.dataManager().getBanned();

        int lastPage = banned.size() / headsPerPage;

        if (page < 0 || page > lastPage) throw new IllegalArgumentException("Invalid ReviverMenu page '" + page + "'");

        // Adding the arrows
        if (page != 0) container.setItem(21, createArrow(page - 1));
        if (page != lastPage) container.setItem(23, createArrow(page + 1));

        // Adding the heads
        for (int i = 0; i < headsPerPage; i++) {
            int index = i + headsPerPage * page;
            if (banned.size() <= index) return;

            container.setItem(i + 10, createHead(banned.get(index)));
        }
    }

    public ItemStack createHead(NameAndId player) {
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);

        GameProfile profile = infuse.getProfile(player);

        skull.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        skull.set(DataComponents.CUSTOM_NAME, Component.literal(player.name()).withStyle(Style.EMPTY.withColor(TextColor.YELLOW).withItalic(false)));

        return skull;
    }

    public ItemStack createArrow(int dest) {
        ItemStack arrow = new ItemStack(Items.FEATHER);

        CompoundTag tag = new CompoundTag();
        tag.putInt(DEST_KEY.toString(), dest);

        arrow.set(DataComponents.CUSTOM_NAME, Component.literal("To page " + dest));
        arrow.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return arrow;
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, @NonNull ContainerInput containerInput, @NonNull Player player) {
        // Ignoring all clicks other than regular left clicks
        if (containerInput != ContainerInput.PICKUP) return;

        ItemStack clicked = container.getItem(slotIndex);

        // Ignoring if nothing was clicked
        if (clicked.isEmpty()) return;

        // Handling when a feather is clicked
        if (clicked.is(Items.FEATHER)) {
            CustomData data = clicked.get(DataComponents.CUSTOM_DATA);
            if (data == null) return;

            int page = data.copyTag().getIntOr(DEST_KEY.toString(), -1);
            if (page == -1) return;

            // Go to new page
            shouldRefund = false;
            player.openMenu(new ReviverMenuProvider(page));
            return;
        }

        // Handling when a head is clicked
        if (clicked.is(Items.PLAYER_HEAD)) {
            ResolvableProfile profile = clicked.get(DataComponents.PROFILE);
            if (profile == null) return;

            GameProfile tmpProfile = profile.partialProfile();

            shouldRefund = false;
            player.openMenu(new ConfirmReviveMenuProvider(page, tmpProfile));
        }
    }

    public void onClose(ServerPlayer player) {
        if (!shouldRefund) return;

        player.addItem(new Reviver().createItem());
    }
}
