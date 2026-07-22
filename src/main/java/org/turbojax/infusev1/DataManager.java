package org.turbojax.infusev1;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
        if (!plugin.isEnabled()) {
            Infuse.LOGGER.error("Infuse not loaded, cannot load {}.", file.getName());
            return false;
        }

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
        // Getting a plugin instance to use
        if (!plugin.isEnabled()) {
            Infuse.LOGGER.error("Infuse not loaded, cannot save the {}.", file.getName());
            return false;
        }

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
        // Getting a plugin instance to use
        if (!plugin.isEnabled()) {
            Infuse.LOGGER.error("Infuse not loaded, cannot create default {}.", file.getName());
            return false;
        }

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

    public int getScore(Player player) {
        String key = player.getUniqueId() + ".score";
        if (!config.contains(key)) {
            int score = MainConfig.startingScore();
            config.set(key, score);
            return score;
        }

        return config.getInt(player.getUniqueId().toString() + ".score");
    }

    public void setScore(Player player, int score) {
        config.set(player.getUniqueId() + ".score", score);
    }

    public @NonNull List<@NonNull PotionEffectType> getEffects(Player player) {
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

    public void setEffects(Player player, List<PotionEffectType> effects) {
        List<String> effectKeys = effects.stream()
            .map(PotionEffectType::getKey)
            .map(NamespacedKey::asString)
            .toList();

        config.set(player.getUniqueId() + ".effects", effectKeys);
    }

    /**
     * Adds a positive effect to the player.
     * Automatically increments the player's score.
     * Does not actually check if the provided effect is positive or negative.  This distinction is only for adjusting the player's score.
     * 
     * @param player the player to give an effect to.
     * @param type The PotionEffectType to add.
     */
    public void addPositiveEffect(Player player, PotionEffectType type) {
        List<PotionEffectType> effects = getEffects(player);
        if (effects.add(type)) {
            setEffects(player, effects);
            setScore(player, getScore(player) + 1);
        } else {
            Infuse.LOGGER.warn("Something tried equipping the {} effect to {} but they already have it.", type.getKey().asString(), player.getName());
        }
    }

    public void addNegativeEffect(Player player, PotionEffectType type) {
        List<PotionEffectType> effects = getEffects(player);
        if (effects.add(type)) {
            setEffects(player, effects);
            setScore(player, getScore(player) - 1);
        } else {
            Infuse.LOGGER.warn("Something tried equipping the {} effect to {} but they already have it.", type.getKey().asString(), player.getName());
        }
    }

    public void removePositiveEffect(Player player, PotionEffectType type) {
        List<PotionEffectType> effects = getEffects(player);
        if (effects.remove(type)) {
            setEffects(player, effects);
            setScore(player, getScore(player) - 1);
        } else {
            Infuse.LOGGER.warn("Something tried removing the {} effect from {} but they already don't have it.", type.getKey().asString(), player.getName());
        }
    }

    public void removeNegativeEffect(Player player, PotionEffectType type) {
        List<PotionEffectType> effects = getEffects(player);
        if (effects.remove(type)) {
            setEffects(player, effects);
            setScore(player, getScore(player) + 1);
        } else {
            Infuse.LOGGER.warn("Something tried removing the {} effect from {} but they already don't have it.", type.getKey().asString(), player.getName());
        }
    }

    public void applyUpdates() {}
}