package org.turbojax.infusev1;

import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@NullMarked
public abstract class MutableConfig extends ImmutableConfig {
    protected MutableConfig(Path file) {
        super(file);
    }

    /** Writes the config to the save file.  Doesn't log successes. */
    public void save() {
        save(true);
    }

    /**
     * Writes the config to the file.
     *
     * @param quiet If false, prints a success message to the logger.  Otherwise, stays quiet.
     */
    public void save(boolean quiet) {
        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        if (!Files.exists(file)) createFile();

        // Saving the config
        try {
            loader.save(root);
            if (!quiet) Infuse.LOGGER.info("Saved {}", file.getFileName());
        } catch (ConfigurateException e) {
            Infuse.LOGGER.warn("Could not save {}.", file.getFileName(), e);
        }
    }

    protected <T> void set(ConfigurationNode node, T value) {
        try {
            node.set(value);
        } catch (SerializationException e) {
            Infuse.LOGGER.error("Failed to set value at '{}' in {}", node.key(), file.getFileName(), e);
        }
    }

    protected <T> void setList(ConfigurationNode node, Class<T> clazz, List<T> value) {
        try {
            node.setList(clazz, value);
        } catch (SerializationException e) {
            Infuse.LOGGER.error("Failed to set value at '{}' in {}", node.key(), file.getFileName(), e);
        }
    }

    void applyUpdates() {}
}
