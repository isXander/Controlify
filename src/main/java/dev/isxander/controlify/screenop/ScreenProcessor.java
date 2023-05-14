package dev.isxander.controlify.screenop;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.TabNavigationBarAccessor;
import dev.isxander.controlify.sound.ControlifySounds;
import dev.isxander.controlify.utils.NavigationHelper;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ScreenProcessor<T extends Screen> {
    public final T screen;
    protected final NavigationHelper navigationHelper = new NavigationHelper(10, 3);
    protected static final Minecraft minecraft = Minecraft.getInstance();

    public ScreenProcessor(T screen) {
        this.screen = screen;
        ControlifyEvents.VIRTUAL_MOUSE_TOGGLED.register(this::onVirtualMouseToggled);
    }

    public void onControllerUpdate(Controller<?, ?> controller) {
        if (!Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
            if (!handleComponentNavOverride(controller))
                handleComponentNavigation(controller);

            if (!handleComponentButtonOverride(controller))
                handleButtons(controller);
        } else {
            handleScreenVMouse(controller, Controlify.instance().virtualMouseHandler());
        }

        handleTabNavigation(controller);
    }

    public void render(Controller<?, ?> controller, PoseStack poseStack, float tickDelta) {
        var vmouse = Controlify.instance().virtualMouseHandler();
        this.render(controller, poseStack, tickDelta, vmouse.isVirtualMouseEnabled() ? Optional.of(vmouse) : Optional.empty());
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

    protected void handleComponentNavigation(Controller<?, ?> controller) {
        if (screen.getFocused() == null)
            setInitialFocus();

        var focuses = List.copyOf(getFocusTree());

        var accessor = (ScreenAccessor) screen;

        boolean repeatEventAvailable = navigationHelper.canNavigate();

        var bindings = controller.bindings();

        FocusNavigationEvent.ArrowNavigation event = null;
        if (bindings.GUI_NAVI_RIGHT.held() && (repeatEventAvailable || !bindings.GUI_NAVI_RIGHT.prevHeld())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.RIGHT);

            if (!bindings.GUI_NAVI_RIGHT.prevHeld())
                navigationHelper.reset();
        } else if (bindings.GUI_NAVI_LEFT.held() && (repeatEventAvailable || !bindings.GUI_NAVI_LEFT.prevHeld())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.LEFT);

            if (!bindings.GUI_NAVI_LEFT.prevHeld())
                navigationHelper.reset();
        } else if (bindings.GUI_NAVI_UP.held() && (repeatEventAvailable || !bindings.GUI_NAVI_UP.prevHeld())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.UP);

            if (!bindings.GUI_NAVI_UP.prevHeld())
                navigationHelper.reset();
        } else if (bindings.GUI_NAVI_DOWN.held() && (repeatEventAvailable || !bindings.GUI_NAVI_DOWN.prevHeld())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.DOWN);

            if (!bindings.GUI_NAVI_DOWN.prevHeld())
                navigationHelper.reset();
        }

        if (event != null) {
            ComponentPath path = screen.nextFocusPath(event);
            if (path != null) {
                accessor.invokeChangeFocus(path);

                navigationHelper.onNavigate();

                if (Controlify.instance().config().globalSettings().uiSounds)
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ControlifySounds.SCREEN_FOCUS_CHANGE, 1.0F));

                var newFocusTree = getFocusTree();
                while (!newFocusTree.isEmpty() && !focuses.contains(newFocusTree.peek())) {
                    ComponentProcessorProvider.provide(newFocusTree.poll()).onFocusGained(this, controller);
                }
            }
        }
    }

    protected void handleButtons(Controller<?, ?> controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            screen.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);
        }
        if (controller.bindings().GUI_BACK.justPressed()) {
            playClackSound();
            screen.onClose();
        }
    }

    protected void handleScreenVMouse(Controller<?, ?> controller, VirtualMouseHandler vmouse) {

    }

    protected boolean handleComponentButtonOverride(Controller<?, ?> controller) {
        var focusTree = getFocusTree();
        while (!focusTree.isEmpty()) {
            var focused = focusTree.poll();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerButtons(this, controller)) return true;
        }

        return false;
    }

    protected boolean handleComponentNavOverride(Controller<?, ?> controller) {
        var focusTree = getFocusTree();
        while (!focusTree.isEmpty()) {
            var focused = focusTree.poll();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerNavigation(this, controller)) return true;
        }
        return false;
    }

    protected void handleTabNavigation(Controller<?, ?> controller) {
        var nextTab = controller.bindings().GUI_NEXT_TAB.justPressed();
        var prevTab = controller.bindings().GUI_PREV_TAB.justPressed();

        if (nextTab || prevTab) {
            screen.children().stream()
                    .filter(child -> child instanceof TabNavigationBar)
                    .map(TabNavigationBar.class::cast)
                    .findAny()
                    .ifPresent(navBar -> {
                        var accessor = (TabNavigationBarAccessor) navBar;
                        List<Tab> tabs = accessor.getTabs();
                        int currentIndex = tabs.indexOf(accessor.getTabManager().getCurrentTab());

                        int newIndex = currentIndex + (prevTab ? -1 : 1);
                        if (newIndex < 0) newIndex = tabs.size() - 1;
                        if (newIndex >= tabs.size()) newIndex = 0;

                        navBar.selectTab(newIndex, true);
                    });
        }
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

    protected void render(Controller<?, ?> controller, PoseStack poseStack, float tickDelta, Optional<VirtualMouseHandler> vmouse) {

    }

    protected void setInitialFocus() {
        if (screen.getFocused() == null && Controlify.instance().currentInputMode() == InputMode.CONTROLLER && !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
            var accessor = (ScreenAccessor) screen;
            ComponentPath path = screen.nextFocusPath(accessor.invokeCreateArrowEvent(ScreenDirection.DOWN));
            if (path != null) {
                accessor.invokeChangeFocus(path);
                navigationHelper.clearDelay();
            }
        }
    }

    public VirtualMouseBehaviour virtualMouseBehaviour() {
        return VirtualMouseBehaviour.DEFAULT;
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

    public static void playClackSound() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
