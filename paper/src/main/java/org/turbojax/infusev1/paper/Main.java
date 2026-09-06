package org.turbojax.infusev1.paper;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.turbojax.infusev1.Infuse;
import org.turbojax.infusev1.paper.commands.PaperInfuseCommand;
import org.turbojax.infusev1.paper.listeners.*;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        // Initializing main infuse class
        new InfusePaper();

        // Registering the command
        LifecycleEvents.COMMANDS.newHandler(e -> {
             e.registrar().register(PaperInfuseCommand.build("infuse"));
        });

        // Registering listeners
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerItemConsumeListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerPostRespawnListener(), this);
    }

    @Override
    public void onDisable() {
        Infuse.getInstance().dataManager().save(false);
    }
}
