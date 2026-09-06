package org.turbojax.infusev1.fabric.mixins;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.turbojax.infusev1.Infuse;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {
    @Inject(method="initServer", at=@At("TAIL"))
    private void infusev1$reloadRecipes(CallbackInfoReturnable<Boolean> cir) {
        Infuse.getInstance().reloadRecipes();
    }
}
