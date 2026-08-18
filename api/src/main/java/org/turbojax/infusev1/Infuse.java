package org.turbojax.infusev1;

import java.nio.file.Path;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Infuse {
    Logger LOGGER = LoggerFactory.getLogger("InfuseV1");

    MinecraftServer server();

    Path configFile();
    Path dataFile();

    MainConfig config();
    DataManager dataManager();

    void onDeath(ServerPlayer player);

    void postRespawn(ServerPlayer player);

    void onJoin(ServerPlayer player);
}
