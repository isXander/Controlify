package dev.isxander.controlify.compatibility.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.screen.component.CustomFocus;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.screen.vanilla.ScreenAccessor;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ScreenProcessor {
    private static final int REPEAT_DELAY = 3;

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
        var focusTree = getFocusTree();
        while (!focusTree.isEmpty()) {
            var focused = focusTree.poll();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerNavigation(this, controller)) return;
        }

        var accessor = (ScreenAccessor) screen;

        boolean repeatEventAvailable = ++lastMoved >= REPEAT_DELAY;

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
                lastMoved = 0;
            }
        }
    }

    protected void handleButtons(Controller controller) {
        var focusTree = getFocusTree();
        while (!focusTree.isEmpty()) {
            var focused = focusTree.poll();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerButtons(this, controller)) return;
        }

        if (controller.bindings().GUI_PRESS.justPressed())
            screen.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);
        if (controller.bindings().GUI_BACK.justPressed())
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

    protected Queue<GuiEventListener> getFocusTree() {
        if (screen.getFocused() == null) return new ArrayDeque<>();

        var tree = new ArrayDeque<GuiEventListener>();
        var focused = screen.getFocused();
        tree.add(focused);
        while (focused instanceof CustomFocus customFocus) {
            focused = customFocus.getCustomFocus();
            tree.addFirst(focused);
        }

        return tree;
    }
}
