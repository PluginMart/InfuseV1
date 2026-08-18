package org.turbojax.infusev1.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Accessor(value="containerMenu")
    public abstract AbstractContainerMenu getContainerMenu();
}
