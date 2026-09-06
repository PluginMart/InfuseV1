package org.turbojax.infusev1.fabric.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.turbojax.infusev1.Infuse;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method="die", at=@At("HEAD"))
    public void infusev1$onDeath(DamageSource source, CallbackInfo ci) {
        Infuse.getInstance().onDeath(self());
    }

    @Unique
    public ServerPlayer self() {
        return (ServerPlayer) (Object) this;
    }
}
