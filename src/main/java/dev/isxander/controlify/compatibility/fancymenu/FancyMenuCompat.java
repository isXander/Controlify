package dev.isxander.controlify.compatibility.fancymenu;

import de.keksuccino.fancymenu.customization.action.ActionRegistry;

public final class FancyMenuCompat {
    public static void registerActions() {
        ActionRegistry.register(new OpenControllerCarouselAction());
    }
}
