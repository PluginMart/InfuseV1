package org.turbojax.infusev1;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Infuse extends JavaPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger("Infuse");

    public static Infuse getInstance() {
        return JavaPlugin.getPlugin(Infuse.class);
    }

    public void onEnable() {
        LOGGER.info("Infuse S1 Plugin has been enabled!");
    }

    public void onDisable() {
        LOGGER.info("Infuse Plugin has been disabled!");
    }
}