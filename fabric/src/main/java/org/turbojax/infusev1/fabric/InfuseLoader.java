package org.turbojax.infusev1.fabric;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.fabric.util.MutableRecipeManager;
import org.turbojax.infusev1.fabric.util.ReloadableResources;
import org.turbojax.infusev1.items.CustomItem;

import java.nio.file.Path;
import java.util.Optional;

public class InfuseLoader extends Infuse implements DedicatedServerModInitializer {
    public MinecraftServer server;

    @Override
    public void onInitializeServer() {
        // Loading the config/data
        config.load();
        dataManager.load();
    }

    @Override
    public Path configFile() {
        return Path.of("config", "infuse.yml");
    }

    @Override
    public Path dataFile() {
        return Path.of("data", "infuse.yml");
    }

    @Override
    public void reloadRecipes() {
        CustomItem.getRegisteredItems().forEach((key, item) -> {
            CraftingRecipe recipe = item.getRecipe();
            if (recipe == null) return;

            ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, item.id());
            ((MutableRecipeManager) server.getRecipeManager()).addRecipe(new RecipeHolder<>(recipeKey, recipe));
        });

        ((ReloadableResources) server.getPlayerList()).reloadRecipes();
    }

    @Override
    public NameAndId getPlayer(String name) {
        return server.services().nameToIdCache().get(name).orElse(null);
    }

    @Override
    public GameProfile getProfile(NameAndId player) {
        GameProfile defaultProfile = new GameProfile(player.id(), player.name());
        Optional<GameProfile> profile = server.services().profileResolver().fetchById(player.id());

        return profile.orElse(defaultProfile);
    }

    @Override
    public void banPlayer(NameAndId player) {
        UserBanList bans = server.getPlayerList().getBans();
        bans.add(new UserBanListEntry(player, null, null, null, "Ran out of lives!"));

        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player.id());
        if (serverPlayer == null) return;
        serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
    }

    @Override
    public void unbanPlayer(NameAndId player) {
        UserBanList bans = server.getPlayerList().getBans();
        bans.remove(player);
    }


    public boolean hasPermission(CommandSourceStack source, String permission, Permission fallbackPermission) {
        // TODO: Check luckperms

        return source.permissions().hasPermission(fallbackPermission);
    }
}
