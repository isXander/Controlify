package dev.isxander.controlify.compatibility.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.ScreenAccessor;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class ScreenProcessor {
    private static final int REPEAT_DELAY = 5;
    private static final int INITIAL_REPEAT_DELAY = 20;

    public final Screen screen;
    private int lastMoved = 0;

    public ScreenProcessor(Screen screen) {
        this.screen = screen;
    }

    public void onControllerUpdate(Controller controller) {
        handleComponentNavigation(controller);
        handleButtons(controller);
    }

    protected void handleComponentNavigation(Controller controller) {
        if (screen.getFocused() != null) {
            var focused = screen.getFocused();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerNavigation(this, controller)) return;
        }

        var accessor = (ScreenAccessor) screen;

        boolean repeatEventAvailable = ++lastMoved > INITIAL_REPEAT_DELAY;

        var axes = controller.state().axes();
        var prevAxes = controller.prevState().axes();
        var buttons = controller.state().buttons();
        var prevButtons = controller.prevState().buttons();

        FocusNavigationEvent.ArrowNavigation event = null;
        if (axes.leftStickX() > 0.5f && (repeatEventAvailable || prevAxes.leftStickX() <= 0.5f)) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.RIGHT);
        } else if (axes.leftStickX() < -0.5f && (repeatEventAvailable || prevAxes.leftStickX() >= -0.5f)) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.LEFT);
        } else if (axes.leftStickY() < -0.5f && (repeatEventAvailable || prevAxes.leftStickY() >= -0.5f)) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.UP);
        } else if (axes.leftStickY() > 0.5f && (repeatEventAvailable || prevAxes.leftStickY() <= 0.5f)) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.DOWN);
        } else if (buttons.dpadUp() && (repeatEventAvailable || !prevButtons.dpadUp())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.UP);
        } else if (buttons.dpadDown() && (repeatEventAvailable || !prevButtons.dpadDown())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.DOWN);
        } else if (buttons.dpadLeft() && (repeatEventAvailable || !prevButtons.dpadLeft())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.LEFT);
        } else if (buttons.dpadRight() && (repeatEventAvailable || !prevButtons.dpadRight())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.RIGHT);
        }

        if (event != null) {
            ComponentPath path = screen.nextFocusPath(event);
            if (path != null) {
                accessor.invokeChangeFocus(path);
                ComponentProcessorProvider.provide(path.component()).onNavigateTo(this, controller);
                lastMoved = repeatEventAvailable ? INITIAL_REPEAT_DELAY - REPEAT_DELAY : 0;
            }
        }
    }

    protected void handleButtons(Controller controller) {
        if (screen.getFocused() != null) {
            var focused = screen.getFocused();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerButtons(this, controller)) return;
        }

        var buttons = controller.state().buttons();
        var prevButtons = controller.prevState().buttons();

        if (buttons.a() && !prevButtons.a())
            screen.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);
        if (buttons.b() && !prevButtons.b())
            screen.onClose();
    }

    public void onWidgetRebuild() {
        // initial focus
        if (screen.getFocused() == null && Controlify.getInstance().getCurrentInputMode() == InputMode.CONTROLLER) {
            var accessor = (ScreenAccessor) screen;
            ComponentPath path = screen.nextFocusPath(accessor.invokeCreateArrowEvent(ScreenDirection.DOWN));
            if (path != null)
                accessor.invokeChangeFocus(path);
        }
    }
}
