package org.turbojax.infusev1.fabric.mixins;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.fabric.util.ReloadableResources;

import java.util.List;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements ReloadableResources {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    public abstract void broadcastAll(Packet<?> packet);

    @Shadow
    @Final
    private LayeredRegistryAccess<RegistryLayer> registries;

    @Shadow
    @Final
    private List<ServerPlayer> players;

    @Inject(method="placeNewPlayer", at=@At("TAIL"))
    private void infusev1$handlePlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        Infuse.getInstance().onJoin(player);
    }

    @Inject(method="respawn", at=@At("TAIL"))
    private void infusev1$handlePlayerRespawn(ServerPlayer serverPlayer, boolean keepAllPlayerData, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        if (!keepAllPlayerData) Infuse.getInstance().postRespawn(cir.getReturnValue());
    }

    @Unique
    @Override
    public void infusev1$reloadResources() {
        infusev1$reloadAdvancements();
        infusev1$reloadRecipes();
        infusev1$reloadTagData();
    }

    @Unique
    @Override
    public void infusev1$reloadAdvancements() {
        for(ServerPlayer player : players) {
            player.getAdvancements().reload(server.getAdvancements());
            player.getAdvancements().flushDirty(player, false);
        }
    }

    @Unique
    @Override
    public void infusev1$reloadRecipes() {
        RecipeManager recipeManager = this.server.getRecipeManager();
        ClientboundUpdateRecipesPacket recipes = new ClientboundUpdateRecipesPacket(recipeManager.getSynchronizedItemProperties(), recipeManager.getSynchronizedStonecutterRecipes());

        for(ServerPlayer player : this.players) {
            player.connection.send(recipes);
            player.getRecipeBook().sendInitialRecipeBook(player);
        }
    }

    @Unique
    @Override
    public void infusev1$reloadTagData() {
        this.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
    }
}
