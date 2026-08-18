package org.turbojax.infusev1.inventories;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

public class ConfirmReviveMenuProvider implements MenuProvider {
    private static final Component NAME = Component.literal("Confirm Revive");

    private final int page;
    private final GameProfile profile;

    public ConfirmReviveMenuProvider(int page, GameProfile profile) {
        this.page = page;
        this.profile = profile;
    }

    @Override
    public Component getDisplayName() {
        return NAME;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ConfirmReviveMenu(containerId, inventory, page, profile);
    }
}
