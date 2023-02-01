package dev.isxander.controlify.compatibility.screen.component;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface ComponentProcessorProvider {
    ComponentProcessor componentProcessor();

    static ComponentProcessor provide(GuiEventListener component) {
        if (component instanceof ComponentProcessorProvider provider)
            return provider.componentProcessor();
        return ComponentProcessor.EMPTY;
    }
}
