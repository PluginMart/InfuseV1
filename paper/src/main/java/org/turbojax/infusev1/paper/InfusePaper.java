package org.turbojax.infusev1.paper;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.mojang.authlib.GameProfile;
import io.papermc.paper.ban.BanListType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftOfflinePlayer;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jspecify.annotations.Nullable;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.items.CustomItem;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Date;

public class InfusePaper extends Infuse {
    public InfusePaper() {
        config.load();
        dataManager.load();
    }

    @Override
    public Path configFile() {
        return Path.of("plugins", "Infuse", "config.yml");
    }

    @Override
    public Path dataFile() {
        return Path.of("plugins", "Infuse", "playerdata.yml");
    }

    @Override
    public void reloadRecipes() {
        CustomItem.getRegisteredItems().forEach((_, item) -> {
            CraftingRecipe recipe = item.getRecipe();
            if (recipe == null) return;

            Bukkit.addRecipe(recipe.toBukkitRecipe(CraftNamespacedKey.fromMinecraft(item.id())));
        });
    }

    @Override
    public @Nullable NameAndId getPlayer(String name) {
        CraftOfflinePlayer cop = (CraftOfflinePlayer) Bukkit.getOfflinePlayer(name);

        try {
            Field nameAndId = cop.getClass().getDeclaredField("nameAndId");
            nameAndId.setAccessible(true);
            return (NameAndId) nameAndId.get(cop);
        } catch (Exception e) {
            Infuse.LOGGER.error("Failed to resolve {}'s id", name, e);
        }

        return null;
    }

    @Override
    public GameProfile getProfile(NameAndId player) {
        return CraftPlayerProfile.asAuthlibCopy(Bukkit.getOfflinePlayer(player.id()).getPlayerProfile());
    }

    @Override
    public void banPlayer(NameAndId player) {
        Bukkit.getOfflinePlayer(player.id()).ban("Ran out of lives", (Date) null, null);
    }

    @Override
    public void unbanPlayer(NameAndId player) {
        Bukkit.getBanList(BanListType.PROFILE).pardon(CraftPlayerProfile.asBukkitCopy(player.toUncompletedGameProfile()));
    }

    @Override
    public boolean hasPermission(CommandSourceStack source, String permission, Permission fallbackPermission) {
        return source.hasPermission(fallbackPermission, permission);
    }
}