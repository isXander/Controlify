package dev.isxander.controlify.screenop;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.event.ControlifyEvents;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.ScreenAccessor;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ScreenProcessor<T extends Screen> {
    public final T screen;
    protected int lastMoved = 0;

    public ScreenProcessor(T screen) {
        this.screen = screen;
        ControlifyEvents.VIRTUAL_MOUSE_TOGGLED.register(this::onVirtualMouseToggled);
    }

    public void onControllerUpdate(Controller controller) {
        if (!Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
            handleComponentNavigation(controller);
            handleButtons(controller);
        } else {
            handleVMouseNavigation(controller);
        }
    }

    public void onInputModeChanged(InputMode mode) {
        switch (mode) {
            case KEYBOARD_MOUSE -> ((ScreenAccessor) screen).invokeClearFocus();
            case CONTROLLER -> {
                if (!Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
                    setInitialFocus();
                }
            }
        }
    }

    protected void handleComponentNavigation(Controller controller) {
        if (screen.getFocused() == null)
            setInitialFocus();

        var focusTree = getFocusTree();
        var focuses = List.copyOf(focusTree);
        while (!focusTree.isEmpty()) {
            var focused = focusTree.poll();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerNavigation(this, controller)) return;
        }

        var accessor = (ScreenAccessor) screen;

        boolean repeatEventAvailable = ++lastMoved >= controller.config().screenRepeatNavigationDelay;

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
                lastMoved = 0;

                var newFocusTree = getFocusTree();
                while (!newFocusTree.isEmpty() && !focuses.contains(newFocusTree.peek())) {
                    ComponentProcessorProvider.provide(newFocusTree.poll()).onFocusGained(this, controller);
                }
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

    protected void handleVMouseNavigation(Controller controller) {

    }

    public void onWidgetRebuild() {
        setInitialFocus();
    }

    public void onVirtualMouseToggled(boolean enabled) {
        if (enabled) {
            ((ScreenAccessor) screen).invokeClearFocus();
        } else {
            setInitialFocus();
        }
    }

    protected void setInitialFocus() {
        if (screen.getFocused() == null && Controlify.instance().currentInputMode() == InputMode.CONTROLLER && !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
            var accessor = (ScreenAccessor) screen;
            ComponentPath path = screen.nextFocusPath(accessor.invokeCreateArrowEvent(ScreenDirection.DOWN));
            if (path != null) {
                accessor.invokeChangeFocus(path);
                lastMoved = 0;
            }
        }
    }

    public boolean forceVirtualMouse() {
        return false;
    }

    protected Queue<GuiEventListener> getFocusTree() {
        if (screen.getFocused() == null) return new ArrayDeque<>();

        var tree = new ArrayDeque<GuiEventListener>();
        var focused = screen.getFocused();
        tree.add(focused);
        while (focused instanceof CustomFocus customFocus) {
            focused = customFocus.getCustomFocus();

            if (focused != null)
                tree.addFirst(focused);
        }

        return tree;
    }
}
