package org.turbojax.infusev1;

import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class ImmutableConfig {
    protected final Infuse plugin;
    protected final Path file;
    protected final YamlConfigurationLoader loader;

    protected ConfigurationNode root;

    protected ImmutableConfig(Path file) {
        plugin = Infuse.getInstance();
        this.file = file;

        loader = YamlConfigurationLoader.builder()
                .path(file)
                .indent(2)
                .build();
    }

    /** Reloads the configuration. */
    public void load() {
        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        if (!Files.exists(file)) createFile();

        // Loading the config
        try {
            root = loader.load();
        } catch (ConfigurateException e) {
            Infuse.LOGGER.warn("Encountered an error while parsing {}", file.getFileName(), e);
        }
    }

    /**
     * Creating the file. If it doesn't exist, it creates an empty file.
     *
     * @return True if the file was created successfully.  False otherwise.
     */
    public boolean createFile() {
        // Creating the file if it doesn't exist.
        if (Files.exists(file)) return true;

        try {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not create {}.  Make sure the user has the right permissions.", file.getFileName());
            return false;
        }
    }

    @Nullable
    protected <T> T get(ConfigurationNode node, Class<T> type) {
        try {
            return node.get(type);
        } catch (SerializationException e) {
            Infuse.LOGGER.error("Failed to get value at '{}' in {}", node.key(), file.getFileName(), e);
            return null;
        }
    }

    protected <T> List<T> getList(ConfigurationNode node, Class<T> type) {
        try {
            return node.getList(type, List.of());
        } catch (SerializationException e) {
            Infuse.LOGGER.error("Failed to get value at '{}' in {}", node.key(), file.getFileName(), e);
            return List.of();
        }
    }
}
