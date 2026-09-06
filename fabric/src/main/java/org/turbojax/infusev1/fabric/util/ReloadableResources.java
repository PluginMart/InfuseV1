package org.turbojax.infusev1.fabric.util;

public interface ReloadableResources {

    default void infusev1$reloadResources() {
        infusev1$reloadAdvancements();
        infusev1$reloadRecipes();
        infusev1$reloadTagData();
    }

    void infusev1$reloadAdvancements();
    void infusev1$reloadRecipes();
    void infusev1$reloadTagData();
}
