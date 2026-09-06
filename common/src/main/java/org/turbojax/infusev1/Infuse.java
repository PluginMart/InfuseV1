package org.turbojax.infusev1;

import java.nio.file.Path;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.turbojax.infusev1.items.CustomItem;
import org.turbojax.infusev1.items.GoodPotion;
import org.turbojax.infusev1.items.InfuseEffect;

public abstract class Infuse {
    public static final Logger LOGGER = LoggerFactory.getLogger("InfuseV1");
    private static Infuse instance;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("infusev1", path);
    }

    protected final MainConfig config;
    protected final DataManager dataManager;

    protected Infuse() {
        if (instance != null) throw new IllegalStateException("Cannot load infuse twice");

        instance = this;

        // Registering items
        CustomItem.register(new GoodPotion(), InfuseEffect.EMPTY);

        // Loading configs
        config = new MainConfig();
        dataManager = new DataManager();
    }

    /** Gets an instance of the {@link Infuse} plugin. */
    @NonNull
    public static Infuse getInstance() {
        if (instance == null) throw new IllegalStateException("Could not retrieve infuse instance.  It hasn't been initialized.");

        return instance;
    }

    /** Gets the path to save the config file to. */
    public abstract Path configFile();

    /** Gets the path to save the data file to. */
    public abstract Path dataFile();

    /** Gets an instance of the {@link MainConfig}. */
    public MainConfig config() {
        return config;
    }

    /** Gets an instance of the {@link DataManager}. */
    public DataManager dataManager() {
        return dataManager;
    }

    public abstract void reloadRecipes();

    /**
     * Updates the dead player's score and gives them a random negative effect.<br>
     * Bans the dead player if necessary.<br>
     * If the player died to another player, updates the killer's score and gives them a random positive effect.<br>
     *
     * @param dead The {@link ServerPlayer} who died
     */
    public void onDeath(ServerPlayer dead) {
        int deadScore = dataManager.getScore(dead);

        Player killer = dead.getLastHurtByPlayer();
        if (killer == null) return;
        if (killer != dead.getKillCredit()) return;

        int killerScore = dataManager.getScore(killer);

        if (deadScore - 1 >= config.minScore()) {
            // Updating the player's score
            dataManager.setScore(dead, deadScore - 1);

            // Banning the player if necessary
            int banScore = config.banScore();
            if (banScore < 0 && deadScore - 1 == banScore) {
                dataManager.ban(dead.nameAndId());
                dead.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }

            if (deadScore > 0 && deadScore <= config.maxPositive()) {
                // Removing a random positive effect
                dataManager.removeRandomEffect(dead);
            } else if (deadScore <= 0 && deadScore > -config.maxNegative()) {
                // Giving a random negative effect
                dataManager.addRandomEffect(dead, false);
            }
        }

        if (killerScore + 1 <= config.maxScore()) {
            // Updating the player's score
            dataManager.setScore(killer, killerScore + 1);

            if (killerScore < 0 && killerScore >= -config.maxNegative()) {
                // Removing a random negative effect
                dataManager.removeRandomEffect(killer);
            } else if (killerScore >= 0 && killerScore < config.maxPositive()) {
                // Giving a random positive effect
                dataManager.addRandomEffect(killer, true);
            }
        }
    }

    /**
     * Re-applies the player's effects when they respawn.
     *
     * @param player The {@link ServerPlayer} who respawned.
     */
    public void postRespawn(ServerPlayer player) {
        // Re-applying the player's effects
        dataManager.getEffects(player).forEach(e -> player.addEffect(new MobEffectInstance(e, -1, config.getEffectiveAmplifier(e))));
    }

    /**
     * Resets the player's effects if necessary.
     *
     * @param player The {@link ServerPlayer} who joined.
     */
    public void onJoin(ServerPlayer player) {
        // Resetting the player's effects as necessary
        if (dataManager.needsReset(player)) {
            dataManager.resetEffects(player);
        }

        // Registering the recipes?
        // TODO: Fix recipes
    }

    /**
     * Gets the {@link NameAndId} of a player.
     *
     * @param name The name of a player.
     */
    @Nullable
    public abstract NameAndId getPlayer(String name);

    /**
     * Gets the {@link GameProfile} of a player.
     * If the profile could not be resolved, it returns the offline mode profile.
     *
     * @param player The player to look up.
     */
    public abstract GameProfile getProfile(NameAndId player);

    /**
     * Handles banning and kicking a player.
     * @param player The player to ban.
     */
    public abstract void banPlayer(NameAndId player);

    /**
     * Handles unbanning a player.
     * @param player The player to unban.
     */
    public abstract void unbanPlayer(NameAndId player);

    /**
     * Checks if the {@link CommandSourceStack} has the specified permission.
     * If the permission is not found, it checks if the source is an admin.
     *
     * @param source The related CommandSourceStack.
     */
    public boolean hasPermission(CommandSourceStack source, String permission) {
        return hasPermission(source, permission, Permissions.COMMANDS_ADMIN);
    }

    /**
     * Checks if the {@link CommandSourceStack} has the specified permission
     * @param source The related CommandSourceStack.
     * @param fallbackPermission The fallback permission to check for if the string permission is not found.
     */
    public abstract boolean hasPermission(CommandSourceStack source, String permission, Permission fallbackPermission);
}
