package org.turbojax.infusev1.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.turbojax.infusev1.Infuse;
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

    @Inject(method="hurtServer", at=@At("TAIL"))
    public void infusev1$onDeath(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        Infuse.getInstance().onDeath(self());
    }

    @Unique
    public ServerPlayer self() {
        return (ServerPlayer) (Object) this;
    }
}
