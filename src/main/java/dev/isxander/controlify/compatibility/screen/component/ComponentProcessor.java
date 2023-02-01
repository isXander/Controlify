package dev.isxander.controlify.compatibility.screen.component;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.controller.AxesState;
import dev.isxander.controlify.controller.ButtonState;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class ComponentProcessor<T extends GuiEventListener> {
    static final ComponentProcessor<?> EMPTY = new ComponentProcessor<>(null);

    protected final T component;

    public ComponentProcessor(T component) {
        this.component = component;
    }

    public boolean overrideControllerNavigation(ScreenProcessor screen, Controller controller) {
        return false;
    }

    public boolean overrideControllerButtons(ScreenProcessor screen, Controller controller) {
        return false;
    }

    public void onNavigateTo(ScreenProcessor screen, Controller controller) {
    }
}
