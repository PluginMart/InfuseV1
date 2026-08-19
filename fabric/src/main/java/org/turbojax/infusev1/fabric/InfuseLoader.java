package org.turbojax.infusev1.fabric;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.item.ItemStack;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.commands.InfuseCommand;
import org.turbojax.infusev1.items.CustomItem;

import java.nio.file.Path;
import java.util.Optional;

public class InfuseLoader extends Infuse implements DedicatedServerModInitializer {
    public MinecraftServer server;

    @Override
    public void onInitializeServer() {
        // Loading the config/data
        config.load();
        dataManager.load();

        // Snagging an instance of the server
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;

            // Registering the command
            server.getCommands().getDispatcher().getRoot().addChild(InfuseCommand.build("infuse"));
        });

        // Saving configs when the server stops
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> dataManager.save());

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
    public Path configFile() {
        return Path.of("config", "infuse.yml");
    }

    @Override
    public Path dataFile() {
        return Path.of("data", "infuse.yml");
    }

    @Override
    public NameAndId getPlayer(String name) {
        return server.services().nameToIdCache().get(name).orElse(null);
    }

    @Override
    public GameProfile getProfile(NameAndId player) {
        GameProfile defaultProfile = new GameProfile(player.id(), player.name());
        Optional<GameProfile> profile = server.services().profileResolver().fetchById(player.id());

        return profile.orElse(defaultProfile);
    }

    @Override
    public void banPlayer(NameAndId player) {
        UserBanList bans = server.getPlayerList().getBans();
        bans.add(new UserBanListEntry(player, null, null, null, "Ran out of lives!"));

        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player.id());
        if (serverPlayer == null) return;
        serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
    }

    @Override
    public void unbanPlayer(NameAndId player) {
        UserBanList bans = server.getPlayerList().getBans();
        bans.remove(player);
    }
}
