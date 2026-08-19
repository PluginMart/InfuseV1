package org.turbojax.infusev1.mixins;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.turbojax.infusev1.Infuse;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method="placeNewPlayer", at=@At("TAIL"))
    private void infusev1_handlePlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        Infuse.getInstance().onJoin(player);
    }

    @Inject(method="respawn", at=@At("TAIL"))
    private void infusev1_handlePlayerRespawn(ServerPlayer serverPlayer, boolean keepAllPlayerData, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        if (!keepAllPlayerData) Infuse.getInstance().postRespawn(cir.getReturnValue());
    }
}
