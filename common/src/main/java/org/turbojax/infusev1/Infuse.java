package org.turbojax.infusev1;

import java.nio.file.Path;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
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
        config.load();
        dataManager = new DataManager();
        dataManager.load();
    }

    /** Gets an instance of the {@link Infuse} plugin. */
    @NonNull
    public static Infuse getInstance() {
        if (instance == null) throw new IllegalStateException("Could not retrieve infuse instance.  It hasn't been initialized.");

        return instance;
    }

    public static Component getEffectName(Holder.Reference<MobEffect> effect) {
        return Component.literal(effect.key().identifier().toShortString().toUpperCase()).withColor(ChatFormatting.YELLOW.getColor());
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

    /** Reloads recipes from the config */
    public abstract void reloadRecipes();

    /**
     * Updates the dead player's score and gives them a random negative effect.<br>
     * Bans the dead player if necessary.<br>
     * If the player died to another player, updates the killer's score and gives them a random positive effect.<br>
     *
     * @param dead The {@link ServerPlayer} who died
     */
    public void onDeath(ServerPlayer dead) {
        if (dead.getKillCredit() == null && !config.loseEffectOnNaturalDeath()) return;

        int deadScore = dataManager.getScore(dead);

        if (deadScore != config.minScore()) {
            // Updating the player's score
            dataManager.setScore(dead, deadScore - 1);

            if (deadScore > 0) {
                // Removing a random positive effect
                dataManager.removeRandomEffect(dead, false);
            } else {
                // Giving a random negative effect
                dataManager.addRandomEffect(dead, false);
            }
        }

        if (!(dead.getKillCredit() instanceof Player killer)) return;
        int killerScore = dataManager.getScore(killer);

        if (deadScore == config.minScore() && !config.getEffectOnMinScore()) return;
        if (killerScore == config.maxScore()) return;

        // Updating the player's score
        dataManager.setScore(killer, killerScore + 1);

        if (killerScore < 0) {
            // Removing a random negative effect
            dataManager.removeRandomEffect(killer, true);
        } else {
            // Giving a random positive effect
            dataManager.addRandomEffect(killer, true);
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
        // Registering the recipes
        player.awardRecipesByKey(CustomItem.getRegisteredItems().values().stream().map(i -> ResourceKey.create(Registries.RECIPE, i.id())).toList());
    }
}
