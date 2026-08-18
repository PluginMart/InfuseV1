package org.turbojax.infusev1;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public class DataManager extends MutableConfig {
    public DataManager() {
        super(InfuseProvider.get().dataFile());
    }

    public int getScore(Player player) {
        return getScore(player.getUUID());
    }

    public int getScore(UUID player) {
        assert root != null;

        ConfigurationNode node = root.node(player.toString(), "score");

        if (node.empty()) {
            int score = plugin.config().startingScore();
            setScore(player, score);
            return getScore(player);
        }

        return node.getInt();
    }

    public void setScore(Player player, int score) {
        setScore(player.getUUID(), score);
    }

    public void setScore(UUID player, int score) {
        assert root != null;

        // Clamping the score within the bounds
        score = Math.clamp(score, plugin.config().minScore(), plugin.config().maxScore());

        // Updating the data
        set(root.node(player.toString(), "score"), score);

        save();
    }

    public List<Holder.Reference<MobEffect>> getEffects(Player player) {
        return getEffects(player.nameAndId());
    }

    public List<Holder.Reference<MobEffect>> getEffects(NameAndId player) {
        assert root != null;
        return getList(root.node(player.id().toString(), "effects"), String.class)
            .stream()
            .map(e -> {
                Optional<Holder.Reference<MobEffect>> type = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(e));
                if (type.isPresent()) return type.get();

                Infuse.LOGGER.warn("Invalid potion effect '{}' in {}'s stored effects. (UUID '{}'", e, player.name(), player.id());
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public void setEffects(UUID player, List<Holder.Reference<MobEffect>> effects) {
        assert root != null;

        List<String> effectKeys = effects.stream()
            .map(Holder.Reference::key)
            .map(ResourceKey::identifier)
            .map(Identifier::toString)
            .toList();


        setList(root.node(player.toString(), "effects"), String.class, effectKeys);

        save();
    }

    /**
     * Adds an effect to the player.
     * Does not actually check if the provided effect is positive or negative.
     * 
     * @param player the player to give an effect to.
     * @param type The PotionEffectType to add.
     */
    public void addEffect(Player player, Holder.Reference<MobEffect> type) {
        assert root != null;

        List<Holder.Reference<MobEffect>> effects = new ArrayList<>(getEffects(player));
        if (effects.contains(type)) {
            Infuse.LOGGER.warn("Something tried equipping the {} effect to {} but they already have it.", type.key().identifier(), player.getName());
            return;
        }


        try {
            root.node(player.getUUID().toString(), "effects").appendListNode().set(type.key().identifier().toString());
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }

        player.addEffect(new MobEffectInstance(type, -1, plugin.config().getEffectiveAmplifier(type)));
    }

    public void removeEffect(Player player, Holder.Reference<MobEffect> type) {
        assert root != null;

        List<Holder.Reference<MobEffect>> effects = new ArrayList<>(getEffects(player.nameAndId()));
        if (effects.remove(type)) {
            setEffects(player.getUUID(), effects);
            player.removeEffect(type);
        } else {
            Infuse.LOGGER.warn("Something tried removing the {} effect from {} but they already don't have it.", type.key().identifier(), player.getName());
        }
    }

    public void addRandomEffect(Player player, boolean positive) {
        assert root != null;

        List<Holder.Reference<MobEffect>> possibleEffects = new ArrayList<>(positive ? plugin.config().positiveEffects() : plugin.config().negativeEffects());

        // Filtering already equipped effects
        possibleEffects.removeAll(getEffects(player.nameAndId()));

        // Selecting a random effect
        Holder.Reference<MobEffect> effect = possibleEffects.get((int)(Math.random() * possibleEffects.size()));

        // Equipping the effect
        addEffect(player, effect);
    }

    public void removeRandomEffect(Player player) {
        assert root != null;

        List<Holder.Reference<MobEffect>> effects = new ArrayList<>(getEffects(player));
        Holder.Reference<MobEffect> removed = effects.remove((int)(Math.random() * effects.size()));

        removeEffect(player, removed);
    }

    public @Unmodifiable List<NameAndId> getBanned() {
        assert root != null;

        return getList(root.node("banned"), String.class)
            .stream()
            .map(s -> {
                String[] parts = s.split(":");
                UUID id = UUID.fromString(parts[1]);
                String name = parts[0];

                return new NameAndId(id, name);
            })
            .toList();
    }

    public void setBanned(List<NameAndId> banned) {
        assert root != null;

        setList(root.node("banned"), String.class, banned.stream()
            .map(n -> "%s:%s".formatted(n.name(), n.id()))
            .toList());

        save();
    }

    public void ban(NameAndId player) {
        assert root != null;

        List<NameAndId> banned = new ArrayList<>(getBanned());

        if (banned.contains(player)) return;

        UserBanList bans = plugin.server().getPlayerList().getBans();
        bans.add(new UserBanListEntry(player, null, null, null, "Ran out of lives!"));

        banned.add(player);
        setBanned(banned);
    }

    public void unban(NameAndId player) {
        assert root != null;

        List<NameAndId> banned = new ArrayList<>(getBanned());
        if (!banned.contains(player)) return;
        banned.remove(player);
        setBanned(banned);

        UserBanList bans = plugin.server().getPlayerList().getBans();
        bans.remove(player);

        setScore(player.id(), plugin.config().reviveScore());

        set(root.node(player.id().toString(), "needs_reset"), true);
        save();
    }

    public boolean needsReset(Player player) {
        assert root != null;

        return root.node(player.getUUID().toString(), "needs_reset").getBoolean(false);
    }

    public void resetEffects(Player player) {
        assert root != null;

        int score = getScore(player);
        if (score > 0) score = Math.min(score, plugin.config().maxPositive());
        else score = Math.max(score, -plugin.config().maxNegative());

        // Getting the effects to give the player
        List<Holder.Reference<MobEffect>> newEffects = new ArrayList<>();
        List<Holder.Reference<MobEffect>> possibleEffects = new ArrayList<>((score > 0) ? plugin.config().positiveEffects() : plugin.config().negativeEffects());

        for (int i = 0; i < Math.abs(score); i++) {
            newEffects.add(possibleEffects.remove((int) (Math.random() * possibleEffects.size())));
        }

        // Overriding the player's effects
        setEffects(player.getUUID(), newEffects);

        // Removing all infinite effects
        var toRemove = player.getActiveEffects().stream()
            .filter(e -> e.getDuration() == -1)
            .map(MobEffectInstance::getEffect)
            .toList();

        toRemove.forEach(player::removeEffect);

        // Equipping the new effects
        newEffects.forEach(e -> player.addEffect(new MobEffectInstance(e, -1, plugin.config().getEffectiveAmplifier(e))));

        set(root.node(player.getUUID().toString(), "needs_reset"), false);

        save();
    }
}
