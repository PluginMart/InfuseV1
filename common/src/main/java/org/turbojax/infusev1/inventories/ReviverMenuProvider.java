package org.turbojax.infusev1.inventories;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

public class ReviverMenuProvider implements MenuProvider {
    private static final Component NAME = Component.literal("Revive a player");

    private final int page;

    public ReviverMenuProvider(int page) {
        this.page = page;
    }

    @Override
    public Component getDisplayName() {
        return NAME;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReviverMenu(containerId, inventory, page);
    }
}
