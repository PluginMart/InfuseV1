package org.turbojax.infusev1.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.turbojax.infusev1.inventories.ConfirmReviveMenu;
import org.turbojax.infusev1.inventories.ReviverMenu;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin {
    @Inject(method="doCloseContainer", at=@At("HEAD"))
    public void infusev1_onClose(CallbackInfo ci) {
        AbstractContainerMenu containerMenu = getContainerMenu();

        if (containerMenu instanceof ReviverMenu m) {
            m.onClose(self());
        }

        if (containerMenu instanceof ConfirmReviveMenu m) {
            m.onClose(self());
        }
    }

    @Unique
    public ServerPlayer self() {
        return (ServerPlayer) (Object) this;
    }
}
