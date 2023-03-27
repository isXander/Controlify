package dev.isxander.controlify.screenop;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface ComponentProcessorProvider {
    ComponentProcessor componentProcessor();

    static ComponentProcessor provide(GuiEventListener component) {
        if (component instanceof ComponentProcessorProvider provider)
            return provider.componentProcessor();

        return REGISTRY.get(component).orElse(ComponentProcessor.EMPTY);

    }

    Registry<GuiEventListener, ComponentProcessor> REGISTRY = new Registry<>();
}
