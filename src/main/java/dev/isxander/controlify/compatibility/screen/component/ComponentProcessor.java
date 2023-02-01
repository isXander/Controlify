package dev.isxander.controlify.compatibility.screen.component;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.controller.AxesState;
import dev.isxander.controlify.controller.ButtonState;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface ComponentProcessor {
    ComponentProcessor EMPTY = new ComponentProcessor(){};

    default boolean overrideControllerNavigation(ScreenProcessor screen, Controller controller) {
        return false;
    }

    default boolean overrideControllerButtons(ScreenProcessor screen, Controller controller) {
        return false;
    }

    default void onNavigateTo(ScreenProcessor screen, Controller controller) {
    }
}
