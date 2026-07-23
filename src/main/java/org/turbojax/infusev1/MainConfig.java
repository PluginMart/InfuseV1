package org.turbojax.infusev1;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.turbojax.infusev1.items.CustomItem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

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

        return Math.max(enhancedLevel(), 1);
    }

    @Nullable
    public static CraftingRecipe createRecipe(CustomItem item) {
        String baseKey = "recipes." + item.getKey().getKey();
        if (!config.contains(baseKey)) return null;

        String type = config.getString(baseKey + ".type");

        if (type == null || !(type.equals("shaped") || type.equals("shapeless"))) {
            Infuse.LOGGER.warn("Invalid recipe '{}'.  Invalid or missing 'type' key.  Allowed values are 'shaped' or 'shapeless'.", item.getKey().getKey());
            return null;
        }

        if (type.equals("shaped")) {
            return getShapedRecipe(item);
        } else {
            return getShapelessRecipe(item);
        }
    }

    public static ShapedRecipe getShapedRecipe(CustomItem item) {
        String baseKey = item.getKey().getKey();
        ShapedRecipe recipe = new ShapedRecipe(item.getKey(), item.createItem());

        // Handling missing sections
        if (!config.contains("recipes." + baseKey + ".shape")) {
            Infuse.LOGGER.warn("Shaped recipe '{}' is missing the 'shape' key.", baseKey);
            return null;
        }
        
        if (!config.contains("recipes." + baseKey + ".ingredients")) {
            Infuse.LOGGER.warn("Shaped recipe '{}' is missing the 'ingredients' key.", baseKey);
            return null;
        }

        // Parsing shape
        String[] shape = config.getStringList("recipes." + baseKey + ".shape").stream().toArray(String[]::new);
        if (shape.length != 3 || shape[0].length() != 3 || shape[1].length() != 3 || shape[2].length() != 3) {
            Infuse.LOGGER.warn("Invalid shape config.  It needs to be a list of three strings that are each 3 characters long.");
            return null;
        }

        recipe.shape(shape);

        ConfigurationSection ingredientsConfig = config.getConfigurationSection("recipes." + baseKey + ".ingredients");
        for (String key : ingredientsConfig.getKeys(false)) {
            char ingredientLabel = key.charAt(0);

            String materialName = ingredientsConfig.getString(key);
            if (materialName == null) {
                Infuse.LOGGER.error("The item '{}' has failed to register its recipe.  An ingredient has not been defined properly.", baseKey);
                return null;
            }

            Material ingredientMaterial = Material.valueOf(materialName.toUpperCase());
            recipe.setIngredient(ingredientLabel, ingredientMaterial);
        }

        return recipe;
    }

    public static ShapelessRecipe getShapelessRecipe(CustomItem item) {
        String baseKey = item.getKey().getKey();
        ShapelessRecipe recipe = new ShapelessRecipe(item.getKey(), item.createItem());

        if (!config.contains("recipes." + baseKey + ".ingredients")) {
            Infuse.LOGGER.warn("Shapeless recipe '{}' is missing the 'ingredients' key.", baseKey);
            return null;
        }

        List<String> ingredients = config.getStringList("recipes." + baseKey + ".ingredients");
        for (String ingredient : ingredients) {
            Material mat = Registry.MATERIAL.get(NamespacedKey.fromString(ingredient.toLowerCase()));
            if (mat == null) {
                Infuse.LOGGER.warn("Invalid material '{}' for recipe '{}'", ingredient, baseKey);
                return null;
            }

            recipe.addIngredient(mat);
        }

        return recipe;
    }

    public static void applyUpdates() {}
}
