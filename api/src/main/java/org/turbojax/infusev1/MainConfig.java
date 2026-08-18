package org.turbojax.infusev1;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.turbojax.infusev1.items.CustomItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class MainConfig extends ImmutableConfig {
    public MainConfig() {
        super(InfuseProvider.get().configFile());
    }

    @Override
    public boolean createFile() {
        // Creating the file if it doesn't exist.
        if (Files.exists(file)) return true;

        try {
            Files.createDirectories(file.getParent());

            InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml");
            assert in != null;
            Files.copy(in, file);
            in.close();

            in = this.getClass().getClassLoader().getResourceAsStream("config.yml");
            assert in != null;
            Files.copy(in, file.getParent().resolve("config2.yml"));
            in.close();
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not create {}.  Make sure the user has the right permissions.", file.getFileName());
            return false;
        }
    }

    public int startingScore() {
        assert root != null;

        return root.node("starting_score").getInt(0);
    }

    public int maxScore() {
        assert root != null;

        return root.node("max_score").getInt(8);
    }

    public int minScore() {
        assert root != null;

        return root.node("min_score").getInt(-9);
    }

    public int banScore() {
        assert root != null;

        return root.node("ban_score").getInt(-9);
    }

    public int reviveScore() {
        assert root != null;

        return root.node("revive_score").getInt(0);
    }

    public int maxPositive() {
        assert root != null;

        return root.node("max_positive").getInt(8);
    }

    public int maxNegative() {
        assert root != null;

        return root.node("max_negative").getInt(8);
    }

    public List<Holder.Reference<MobEffect>> positiveEffects() {
        assert root != null;

        return getList(root.node("positive_effects"), String.class)
            .stream()
            .map(s -> {
                Optional<Holder.Reference<MobEffect>> e = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(s));
                if (e.isPresent()) return e.get();

                Infuse.LOGGER.warn("Invalid positive effect key '{}'", s);
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public List<Holder.Reference<MobEffect>> negativeEffects() {
        assert root != null;

        return getList(root.node("negative_effects"), String.class)
            .stream()
            .map(s -> {
                Optional<Holder.Reference<MobEffect>> e = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(s));
                if (e.isPresent()) return e.get();

                Infuse.LOGGER.warn("Invalid negative effect key '{}'", s);
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public int effectLevel() {
        assert root != null;

        return root.node("effect_level").getInt(1);
    }

    public int getEffectiveAmplifier(Holder.Reference<MobEffect> effect) {
        assert root != null;

        ConfigurationNode overrides = root.node("override_levels");
        if (overrides.hasChild(effect.key().identifier())) return Math.max(overrides.node(effect.key().identifier()).getInt() - 1, 0);
        if (overrides.hasChild(effect.key().identifier().toShortString())) return Math.max(overrides.node(effect.key().identifier().toShortString()).getInt() - 1, 0);

        return Math.max(effectLevel() - 1, 0);
    }

    public int enhancerDuration() {
        assert root != null;

        return root.node("enhancer_duration").getInt(90);
    }

    public int enhancedLevel() {
        assert root != null;

        return root.node("enhanced_level").getInt(3);
    }

    public int getEffectiveEnhancedAmplifier(Holder.Reference<MobEffect> effect) {
        assert root != null;

        ConfigurationNode overrides = root.node("enhanced_override_levels");
        if (overrides.hasChild(effect.key().identifier())) return Math.max(overrides.node(effect.key().identifier()).getInt() - 1, 0);
        if (overrides.hasChild(effect.key().identifier().toShortString())) return Math.max(overrides.node(effect.key().identifier().toShortString()).getInt() - 1, 0);

        return Math.max(enhancedLevel() - 1, 0);
    }

    @Nullable
    public CraftingRecipe getRecipe(CustomItem item) {
        assert root != null;

        ConfigurationNode recipes = root.node("recipes");

        String baseKey = item.key();
        if (!recipes.hasChild(baseKey)) return null;

        String type = recipes.node(baseKey, "type").getString();

        if (type != null && type.equals("shaped")) {
            return getShapedRecipe(item);
        } else if (type != null && type.equals("shapeless")) {
            return getShapelessRecipe(item);
        }

        Infuse.LOGGER.warn("Invalid recipe '{}'.  Invalid or missing 'type' key.  Allowed values are 'shaped' or 'shapeless'.", baseKey);
        return null;
    }

    public ShapedRecipe getShapedRecipe(CustomItem item) {
        String baseKey = item.key();

        assert root != null;
        ConfigurationNode recipeNode = root.node("recipes", baseKey);

        // Verifying schema
        if (!recipeNode.hasChild("shape")) {
            Infuse.LOGGER.warn("Shaped recipe '{}' is missing the 'shape' key.", baseKey);
            return null;
        }

        if (!recipeNode.hasChild("ingredients")) {
            Infuse.LOGGER.warn("Shaped recipe '{}' is missing the 'ingredients' key.", baseKey);
            return null;
        }

        // Parsing shape
        List<String> shape = getList(recipeNode.node("shape"), String.class);

        // Parsing ingredients
        Map<Character,Ingredient> key = new HashMap<>();
        ConfigurationNode ingredientsConfig = recipeNode.node("ingredients");
        ingredientsConfig.childrenMap().forEach((k, node) -> {
            // Validating the length of the key
            String s = (String) k;
            if (s.length() != 1) {
                Infuse.LOGGER.error("Invalid ingredient key '{}' in recipe '{}'.  Keys must be 1 character.", k, baseKey);
                return;
            }

            // Getting the value of the node
            String value = node.getString("");
            Ingredient ingredient = parseIngredient(value, baseKey);

            // Logging is handled already
            if (ingredient == null) return;

            key.put(s.charAt(0), ingredient);
        });

        ShapedRecipePattern pattern = ShapedRecipePattern.of(key, shape);
        return new net.minecraft.world.item.crafting.ShapedRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""), pattern, item.createTemplate());
    }

    public ShapelessRecipe getShapelessRecipe(CustomItem item) {
        assert root != null;

        String baseKey = item.key();

        ConfigurationNode recipeNode = root.node("recipes", baseKey);

        if (!recipeNode.hasChild("ingredients")) {
            Infuse.LOGGER.warn("Shapeless recipe '{}' is missing the 'ingredients' key.", baseKey);
            return null;
        }

        List<Ingredient> ingredients = getList(root.node("recipes", baseKey, "ingredients"), String.class)
                .stream()
                .map(k -> parseIngredient(k, baseKey))
                .filter(Objects::nonNull)
                .toList();

        return new net.minecraft.world.item.crafting.ShapelessRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""), item.createTemplate(), ingredients);
    }

    @Nullable
    public Ingredient parseIngredient(String key, String recipeKey) {
        // Handling tag keys
        if (key.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(key.substring(1)));
            Optional<HolderSet.Named<Item>> items = BuiltInRegistries.ITEM.get(tag);

            if (items.isEmpty()) {
                Infuse.LOGGER.error("Invalid item tag '{}' in recipe '{}'", key, recipeKey);
                return null;
            }

            return Ingredient.of(items.get());
        }

        // Getting the item
        Holder.Reference<Item> item = BuiltInRegistries.ITEM.get(Identifier.parse(key)).orElse(null);

        if (item == null) {
            Infuse.LOGGER.error("Invalid item type '{}' in recipe '{}'.", key, recipeKey);
            return null;
        }

        return Ingredient.of(item.value());
    }
}
