package org.turbojax.infusev1;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class MainConfig {
    private static final Infuse plugin = Infuse.getInstance();
    private static final File file = new File(plugin.getDataFolder(), "config.yml");
    private static final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    /**
     * Reloads the configuration.
     *
     * @return Whether the configuration was loaded successfully.
     */
    public static boolean load() {
        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), true);
        }

        // Loading the config
        try {
            config.load(file);
            Infuse.LOGGER.info("Successfully loaded {}", file.getName());
            return true;
        } catch (InvalidConfigurationException e) {
            Infuse.LOGGER.warn("{} contains an invalid YAML configuration.  Verify the contents of the file.", file.getName());
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not find {}.  Check that it exists.", file.getName());
            e.printStackTrace();
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
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
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

    public static int startingScore() {
        return config.getInt("starting_score", 0);
    }

    public static int maxScore() {
        return config.getInt("max_score", 8);
    }

    public static int minScore() {
        return config.getInt("min_score", -9);
    }

    public static int banScore() {
        return config.getInt("ban_score", -9);
    }

    public static int reviveScore() {
        return config.getInt("revive_score", 0);
    }

    public static int maxPositive() {
        return config.getInt("max_positive", 8);
    }

    public static int maxNegative() {
        return config.getInt("max_negative", 8);
    }

    public static List<PotionEffectType> positiveEffects() {
        return config.getStringList("positive_effects")
            .stream()
            .map(String::toLowerCase)
            .map(s -> {
                PotionEffectType e = Registry.EFFECT.get(NamespacedKey.fromString(s.toLowerCase()));
                if (e == null) Infuse.LOGGER.warn("Invalid positive effect key '{}'", s);
                return e;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public static List<PotionEffectType> negativeEffects() {
        return config.getStringList("negative_effects")
            .stream()
            .map(String::toLowerCase)
            .map(s -> {
                PotionEffectType e = Registry.EFFECT.get(NamespacedKey.fromString(s.toLowerCase()));
                if (e == null) Infuse.LOGGER.warn("Invalid negative effect key '{}'", s);
                return e;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public static int effectLevel() {
        return config.getInt("effect_level", 1);
    }

    public static int getEffectiveLevel(PotionEffectType effect) {
        if (config.contains("override_levels." + effect.getKey().asString())) return Math.max(config.getInt("override_levels." + effect.getKey().asString()), 1);
        if (config.contains("override_levels." + effect.getKey().asMinimalString())) return Math.max(config.getInt("override_levels." + effect.getKey().asMinimalString()), 1);

        return Math.max(effectLevel(), 1);
    }

    public static int enhancerDuration() {
        return config.getInt("enhancer_duration", 90);
    }

    public static int enhancedLevel() {
        return config.getInt("enhanced_level", 3);
    }

    public static int getEffectiveEnhancedLevel(PotionEffectType effect) {
        if (config.contains("enhanced_override_levels." + effect.getKey().asString())) return Math.max(config.getInt("enhanced_override_levels." + effect.getKey().asString()), 1);
        if (config.contains("enhanced_override_levels." + effect.getKey().asMinimalString())) return Math.max(config.getInt("enhanced_override_levels." + effect.getKey().asMinimalString()), 1);

        return Math.max(effectLevel(), 1);
    }

    public static CraftingRecipe createRecipe(String key, ItemStack result) {
        if (!config.contains("recipes." + key)) return null;

        ConfigurationSection recipeSection = config.getConfigurationSection("recipes." + key);

        String type = recipeSection.getString("type");

        if (type == null || !(type.equals("shaped") || type.equals("shapeless"))) {
            Infuse.LOGGER.warn("Invalid recipe '{}'.  Invalid or missing 'type' key.  Allowed values are 'shaped' or 'shapeless'.", key);
            return null;
        }

        
        if (type.equals("shaped")) {
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, key), result);

            // Handling missing sections
            if (!recipeSection.contains("shape")) {
                Infuse.LOGGER.warn("Shaped recipe '{}' is missing the 'shape' key.");
                return null;
            }
            
            if (!recipeSection.contains("ingredients")) {
                Infuse.LOGGER.warn("Shaped recipe '{}' is missing the 'ingredients' key.");
                return null;
            }

            // Parsing shape
            String[] shape = recipeSection.getStringList("shape").stream().toArray(String[]::new);
            if (shape.length != 3 || shape[0].length() != 3 || shape[1].length() != 3 || shape[2].length() != 3) {
                Infuse.LOGGER.warn("Invalid shape config.  It needs to be a list of three strings that are each 3 characters long.");
                return null;
            }

            recipe.shape(shape);

            String fullShape = String.join("", shape);

            // Parsing ingredients
            ConfigurationSection ingredients = recipeSection.getConfigurationSection("ingredients");
            Stream<Map.Entry<Character,Material>> parts = ingredients.getKeys(false)
                .stream()
                .map(k -> {
                    String matKey = ingredients.getString(k);
                    Material mat = Registry.MATERIAL.get(NamespacedKey.fromString(matKey));
                    if (!fullShape.contains(k)) mat = Material.AIR; // setting unused keys to Material.AIR
                    if (mat != null) return Map.<Character,Material>entry(k.charAt(0), mat);
                    
                    Infuse.LOGGER.warn("Invalid material '{}' for key '{}' in shaped recipe '{}'", matKey, k, key);
                    return null;
                });

            if (parts.filter(Objects::isNull).findAny().isPresent()) {
                Infuse.LOGGER.warn("Cannot create recipe {} due to invalid materials.", key);
                return null;
            }

            parts.filter(e -> {
                    if (e.getValue() != Material.AIR) return true;

                    Infuse.LOGGER.warn("Ignoring material '{}' in recipe '{}' because it is not used.", e.getKey(), key);
                    return false;
                })
                .forEach(e -> recipe.setIngredient(e.getKey(), e.getValue()));

            return recipe;
        } else {
            ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, key), result);

            if (!recipeSection.contains("ingredients")) {
                Infuse.LOGGER.warn("Shapeless recipe '{}' is missing the 'ingredients' key.", key);
                return null;
            }

            Stream<Material> mats = recipeSection.getStringList("ingredients")
                .stream()
                .map(k -> {
                    Material mat = Registry.MATERIAL.get(NamespacedKey.fromString(k));
                    if (mat != null) return mat;

                    Infuse.LOGGER.warn("Invalid material '{}' for recipe '{}'", k, key);
                    return null;
                });

            if (mats.filter(Objects::isNull).findAny().isPresent()) {
                Infuse.LOGGER.warn("Cannot create recipe {} due to invalid materials.", key);
                return null;
            }

            mats.forEach(recipe::addIngredient);

            return recipe;
        }
    }

    public static void applyUpdates() {}
}
