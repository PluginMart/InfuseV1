package org.turbojax.infusev1;

import java.nio.file.Path;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Infuse {
    public static final Logger LOGGER = LoggerFactory.getLogger("InfuseV1");
    private static Infuse instance;

    protected final MainConfig config;
    protected final DataManager dataManager;

    protected Infuse() {
        if (instance != null) throw new IllegalStateException("Cannot load infuse twice");

        instance = this;

        config = new MainConfig();
        dataManager = new DataManager();
    }


    @NonNull
    public static Infuse getInstance() {
        if (instance == null) throw new IllegalStateException("Could not retrieve infuse instance.  It hasn't been initialized.");

        return instance;
    }

    public abstract MinecraftServer server();

    public abstract Path configFile();
    public abstract Path dataFile();

    public MainConfig config() {
        return config;
    }

    public DataManager dataManager() {
        return dataManager;
    }

    public abstract void onDeath(ServerPlayer player);

    public abstract void postRespawn(ServerPlayer player);

    public abstract void onJoin(ServerPlayer player);


}
