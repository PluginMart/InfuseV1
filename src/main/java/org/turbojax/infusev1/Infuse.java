package org.turbojax.infusev1;

import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.turbojax.infusev1.inventories.ConfirmReviveMenu;
import org.turbojax.infusev1.inventories.ReviverMenu;
import org.turbojax.infusev1.items.Enhancer;
import org.turbojax.infusev1.items.InfuseEffect;
import org.turbojax.infusev1.items.Reviver;
import org.turbojax.infusev1.listeners.PlayerDeathListener;

public class Infuse extends JavaPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger("Infuse");

    public static Infuse getInstance() {
        return JavaPlugin.getPlugin(Infuse.class);
    }

    public void onEnable() {
        MainConfig.load();
        DataManager.load();

        // Registering listeners
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new ReviverMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ConfirmReviveMenu(0, Bukkit.getOfflinePlayer("TurboJax07")), this);
        
        LOGGER.info("Infuse S1 Plugin has been enabled!");
    }

    public void onDisable() {
        MainConfig.save();
        DataManager.save();
        LOGGER.info("Infuse Plugin has been disabled!");
    }

    public void registerRecipes() {
        Stream.of(new InfuseEffect().createRecipe(), new Enhancer().createRecipe(), new Reviver().createRecipe())
            .forEach(r -> {
                Bukkit.removeRecipe(r.getKey());
                Bukkit.addRecipe(r);
                Bukkit.getOnlinePlayers().forEach(p -> p.discoverRecipe(r.getKey()));
            });
    }
}