package org.turbojax.infusev1.fabric.util;

public interface ReloadableResources {

    default void reloadResources() {
        reloadAdvancements();
        reloadRecipes();
        reloadTagData();
    }

    void reloadAdvancements();
    void reloadRecipes();
    void reloadTagData();
}
