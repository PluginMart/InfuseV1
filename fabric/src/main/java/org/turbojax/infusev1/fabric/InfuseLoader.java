package org.turbojax.infusev1.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.commands.InfuseCommand;
import org.turbojax.infusev1.items.CustomItem;

import java.nio.file.Path;

public class InfuseLoader extends Infuse implements DedicatedServerModInitializer {
    public static MinecraftServer server;

    @Override
    public void onInitializeServer() {
        // Loading the config/data
        config.load();
        dataManager.load();

        // Snagging an instance of the server
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            InfuseLoader.server = server;

            // Registering the command
            server.getCommands().getDispatcher().getRoot().addChild(InfuseCommand.build("infuse"));
        });

        // Saving configs when the server stops
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            dataManager.save();
        });

        // Passing join events through onJoin
        ServerPlayerEvents.JOIN.register(this::onJoin);

        // Passing respawn events through postRespawn
        ServerPlayerEvents.AFTER_RESPAWN.register((o, newPlayer, alive) -> {
            if (!alive) postRespawn(newPlayer);
        });

        // Passing death events through onDeath
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, s, a) -> {
            if (entity instanceof ServerPlayer p) onDeath(p);

            return true;
        });

        // Listening for the item interaction
        ItemEvents.USE.register((level, player, hand) -> {
            ItemStack item = player.getItemBySlot(hand.asEquipmentSlot());

            CustomItem ci = CustomItem.fromItemStack(item);
            if (ci == null) return null;

            return ci.interact(player, item);
        });
    }

    @Override
    public MinecraftServer server() {
        return server;
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

    @Override
    public void onJoin(ServerPlayer player) {
        // Resetting the player's effects as necessary
        if (dataManager.needsReset(player)) {
            dataManager.resetEffects(player);
        }

        // Registering the recipes?
        // TODO: Fix recipes
    }

    @Override
    public void postRespawn(ServerPlayer player) {
        // Re-applying the player's effects
        dataManager.getEffects(player).forEach(e -> player.addEffect(new MobEffectInstance(e, -1, config.getEffectiveAmplifier(e))));
    }
}
