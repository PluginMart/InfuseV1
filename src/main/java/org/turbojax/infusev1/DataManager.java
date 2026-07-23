package org.turbojax.infusev1;

import io.papermc.paper.ban.BanListType;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataManager {
    private static final Infuse plugin = Infuse.getInstance();
    private static final File file = new File(plugin.getDataFolder(), "data/playerdata.yml");
    private static final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    /**
     * Reloads the configuration.
     *
     * @return Whether the configuration was loaded successfully.
     */
    public static boolean load() {
        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        if (!createFile(false)) {
            return false;
        }

        // Loading the config
        try {
            config.load(file);
            Infuse.LOGGER.info("Successfully loaded {}", file.getName());
            return true;
        } catch (InvalidConfigurationException err) {
            Infuse.LOGGER.warn("{} contains an invalid YAML configuration.  Verify the contents of the file.", file.getName());
        } catch (IOException err) {
            Infuse.LOGGER.error("Could not find {}.  Check that it exists.", file.getName());
        }

        return false;
    }

    /**
     * Writes the config to the file.
     * 
     * @return Whether or not the config was successfully written.
     */
    public static boolean save() {
        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        if (!createFile(false)) {
            return false;
        }

        // Saving the config
        try {
            config.save(file);
            Infuse.LOGGER.info("Saved {}", file.getName());
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.warn("Could not save {}.  Make sure the user has write permissions.", file.getName());
        }

        return false;
    }

    /**
     * Creating the config file. If it doesn't exist, it loads the default config. If the file does
     * exist, it will only replace it if the parameter is true.
     * 
     * @param replace Whether or not to replace the config file with the default configs.
     * @return Whether or not the file was created successfully.
     */
    public static boolean createFile(boolean replace) {
        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                Infuse.LOGGER.error("Could not create {}.  Make sure the user has the right permissions.", file.getName());
                return false;
            }
        }

        return true;
    }

    public static int getScore(OfflinePlayer player) {
        String key = player.getUniqueId() + ".score";
        if (!config.contains(key)) {
            int score = MainConfig.startingScore();
            config.set(key, score);
            return score;
        }

        return config.getInt(player.getUniqueId().toString() + ".score");
    }

    public static void setScore(OfflinePlayer player, int score) {
        // Clamping the score within the bounds
        score = Math.clamp(score, MainConfig.minScore(), MainConfig.maxScore());

        // Updating the data
        config.set(player.getUniqueId() + ".score", score);

        save();
    }

    public static @NonNull List<@NonNull PotionEffectType> getEffects(OfflinePlayer player) {
        return config.getStringList(player.getUniqueId() + ".effects")
            .stream()
            .map(e -> {
                PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.fromString(e));
                if (type != null) return type;

                Infuse.LOGGER.warn("Invalid potion effect '{}' in {}'s stored effects. (UUID '{}'", e, player.getName(), player.getUniqueId());
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public static void setEffects(OfflinePlayer player, List<PotionEffectType> effects) {
        List<String> effectKeys = effects.stream()
            .map(PotionEffectType::getKey)
            .map(NamespacedKey::asString)
            .toList();

        config.set(player.getUniqueId() + ".effects", effectKeys);

        save();
    }

    /**
     * Adds an effect to the player.
     * Does not actually check if the provided effect is positive or negative.
     * 
     * @param player the player to give an effect to.
     * @param type The PotionEffectType to add.
     */
    public static void addEffect(Player player, PotionEffectType type) {
        List<PotionEffectType> effects = new ArrayList<>(getEffects(player));
        if (effects.add(type)) {
            setEffects(player, effects);
            player.addPotionEffect(new PotionEffect(type, -1, MainConfig.getEffectiveLevel(type) - 1));
        } else {
            Infuse.LOGGER.warn("Something tried equipping the {} effect to {} but they already have it.", type.getKey().asString(), player.getName());
        }
    }

    public static void removeEffect(Player player, PotionEffectType type) {
        List<PotionEffectType> effects = new ArrayList<>(getEffects(player));
        if (effects.remove(type)) {
            setEffects(player, effects);
            player.removePotionEffect(type);
        } else {
            Infuse.LOGGER.warn("Something tried removing the {} effect from {} but they already don't have it.", type.getKey().asString(), player.getName());
        }
    }

    public static void addRandomEffect(Player player, boolean positive) {
        List<PotionEffectType> possibleEffects = new ArrayList<>(positive ? MainConfig.positiveEffects() : MainConfig.negativeEffects());

        // Removing already equipped effects
        possibleEffects.removeAll(getEffects(player));

        // Selecting a random effect
        PotionEffectType effect = possibleEffects.get((int)(Math.random() * possibleEffects.size()));

        // Equipping the effect
        addEffect(player, effect);
    }

    public static void removeRandomEffect(Player player) {
        List<PotionEffectType> effects = new ArrayList<>(getEffects(player));
        PotionEffectType removed = effects.remove((int)(Math.random() * effects.size()));

        removeEffect(player, removed);
    }

    public static List<OfflinePlayer> getBanned() {
        return config.getStringList("banned")
            .stream()
            .map(UUID::fromString)
            .map(Bukkit::getOfflinePlayer)
            .collect(Collectors.toList());
    }

    public static void setBanned(List<OfflinePlayer> banned) {
        config.set("banned", banned.stream()
            .map(OfflinePlayer::getUniqueId)
            .map(UUID::toString)
            .toList());

        save();
    }

    public static void ban(OfflinePlayer player) {
        List<OfflinePlayer> banned = getBanned();
        if (banned.add(player)) {
            setBanned(banned);
        }
    }

    public static void unban(OfflinePlayer player) {
        if (player == null) return;
        
        List<OfflinePlayer> banned = getBanned();
        if (banned.remove(player)) {
            setBanned(banned);
            Bukkit.getServer().getBanList(BanListType.PROFILE).pardon(player.getPlayerProfile());
            setScore(player, MainConfig.reviveScore());
        }
    }

    public static void applyUpdates() {}
}