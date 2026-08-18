package org.turbojax.infusev1;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class InfuseProvider {
    @Nullable
    private static Infuse instance;

    public static Infuse get() {
        if (instance == null) throw new IllegalStateException("Could not retrieve infuse instance.  It hasn't been initialized.");

        return instance;
    }

    public static void set(Infuse instance) {
        if (InfuseProvider.instance != null) throw new IllegalStateException("Cannot load infuse twice");

        InfuseProvider.instance = instance;
    }
}
