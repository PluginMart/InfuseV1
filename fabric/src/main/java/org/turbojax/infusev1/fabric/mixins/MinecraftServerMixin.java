package org.turbojax.infusev1.fabric.mixins;

import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.fabric.commands.InfuseCommand;
import org.turbojax.infusev1.fabric.InfuseLoader;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract Commands getCommands();

    @Inject(method="runServer", at= @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initServer()Z"))
    private void infusev1$snagInstance(CallbackInfo ci) {
        // Getting the plugin an instance of the server
        InfuseLoader inst = (InfuseLoader) InfuseLoader.getInstance();
        inst.server = (MinecraftServer) (Object) this;

        // Registering the command
        this.getCommands().getDispatcher().getRoot().addChild(InfuseCommand.build("infuse"));
    }

    @Inject(method="stopServer", at=@At("HEAD"))
    private void infusev1$saveData(CallbackInfo ci) {
        Infuse.getInstance().dataManager().save(false);
    }
}
