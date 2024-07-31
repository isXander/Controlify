package dev.isxander.controlify.screenop;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.dualsense.HapticEffects;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import dev.isxander.controlify.mixins.feature.screenop.vanilla.TabNavigationBarAccessor;
import dev.isxander.controlify.sound.ControlifyClientSounds;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ScreenProcessor<T extends Screen> {
    public final T screen;
    protected final HoldRepeatHelper holdRepeatHelper = new HoldRepeatHelper(10, 3);
    protected static final Minecraft minecraft = Minecraft.getInstance();

    private final List<ScreenControllerEventListener> eventListeners = new ArrayList<>();

    public ScreenProcessor(T screen) {
        this.screen = screen;
        if (screen instanceof ScreenControllerEventListener eventListener) {
            eventListeners.add(eventListener);
        }
    }

    public void onControllerUpdate(ControllerEntity controller) {
        Controlify.instance().virtualMouseHandler().handleControllerInput(controller);

        if (!Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
            if (!handleComponentNavOverride(controller))
                handleComponentNavigation(controller);

            if (!handleComponentButtonOverride(controller))
                handleButtons(controller);
        } else {
            handleScreenVMouse(controller, Controlify.instance().virtualMouseHandler());
        }

        handleTabNavigation(controller);

        eventListeners.forEach(listener -> listener.onControllerInput(controller));
    }

    public void render(ControllerEntity controller, GuiGraphics graphics, float tickDelta) {
        var vmouse = Controlify.instance().virtualMouseHandler();
        this.render(controller, graphics, tickDelta, vmouse.isVirtualMouseEnabled() ? Optional.of(vmouse) : Optional.empty());
    }

    public void onInputModeChanged(InputMode mode) {
        switch (mode) {
            case KEYBOARD_MOUSE -> {
                boolean shouldKeepFocus = getFocusTree().stream()
                        .anyMatch(component -> ComponentProcessorProvider.provide(component).shouldKeepFocusOnKeyboardMode(this));

                if (!shouldKeepFocus) {
                    ((ScreenAccessor) screen).invokeClearFocus();
                }
            }
            case CONTROLLER, MIXED -> {
                if (!Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
                    setInitialFocus();
                }
            }
        }
    }

    protected void handleComponentNavigation(ControllerEntity controller) {
        if (screen.getFocused() == null)
            setInitialFocus();

        var focuses = List.copyOf(getFocusTree());

        var accessor = (ScreenAccessor) screen;

        boolean repeatEventAvailable = holdRepeatHelper.canNavigate();

        InputComponent input = controller.input().orElseThrow();
        ControllerStateView state = input.stateNow();
        ControllerStateView prevState = input.stateThen();

        InputBinding guiNaviRight = ControlifyBindings.GUI_NAVI_RIGHT.on(controller);
        InputBinding guiNaviLeft = ControlifyBindings.GUI_NAVI_LEFT.on(controller);
        InputBinding guiNaviUp = ControlifyBindings.GUI_NAVI_UP.on(controller);
        InputBinding guiNaviDown = ControlifyBindings.GUI_NAVI_DOWN.on(controller);

        FocusNavigationEvent.ArrowNavigation event = null;
        if (guiNaviRight.digitalNow() && (repeatEventAvailable || !guiNaviRight.digitalPrev())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.RIGHT);

            if (!guiNaviRight.digitalPrev())
                holdRepeatHelper.reset();
        } else if (guiNaviLeft.digitalNow() && (repeatEventAvailable || !guiNaviLeft.digitalPrev())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.LEFT);

            if (!guiNaviLeft.digitalPrev())
                holdRepeatHelper.reset();
        } else if (guiNaviUp.digitalNow() && (repeatEventAvailable || !guiNaviUp.digitalPrev())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.UP);

            if (!guiNaviUp.digitalPrev())
                holdRepeatHelper.reset();
        } else if (guiNaviDown.digitalNow() && (repeatEventAvailable || !guiNaviDown.digitalPrev())) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.DOWN);

            if (!guiNaviDown.digitalPrev())
                holdRepeatHelper.reset();
        } else if (state.isButtonDown(GamepadInputs.DPAD_RIGHT_BUTTON) && (repeatEventAvailable || !prevState.isButtonDown(GamepadInputs.DPAD_RIGHT_BUTTON))) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.RIGHT);

            if (!prevState.isButtonDown(GamepadInputs.DPAD_RIGHT_BUTTON))
                holdRepeatHelper.reset();
        } else if (state.isButtonDown(GamepadInputs.DPAD_LEFT_BUTTON) && (repeatEventAvailable || !prevState.isButtonDown(GamepadInputs.DPAD_LEFT_BUTTON))) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.LEFT);

            if (!prevState.isButtonDown(GamepadInputs.DPAD_LEFT_BUTTON))
                holdRepeatHelper.reset();
        } else if (state.isButtonDown(GamepadInputs.DPAD_UP_BUTTON) && (repeatEventAvailable || !prevState.isButtonDown(GamepadInputs.DPAD_UP_BUTTON))) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.UP);

            if (!prevState.isButtonDown(GamepadInputs.DPAD_UP_BUTTON))
                holdRepeatHelper.reset();
        } else if (state.isButtonDown(GamepadInputs.DPAD_DOWN_BUTTON) && (repeatEventAvailable || !prevState.isButtonDown(GamepadInputs.DPAD_DOWN_BUTTON))) {
            event = accessor.invokeCreateArrowEvent(ScreenDirection.DOWN);

            if (!prevState.isButtonDown(GamepadInputs.DPAD_DOWN_BUTTON))
                holdRepeatHelper.reset();
        }

        if (event != null) {
            ComponentPath path = screen.nextFocusPath(event);
            if (path != null) {
                accessor.invokeChangeFocus(path);

                holdRepeatHelper.onNavigate();

                controller.input().ifPresent(InputComponent::notifyGuiPressOutputsOfNavigate);

                if (Controlify.instance().config().globalSettings().uiSounds)
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ControlifyClientSounds.SCREEN_FOCUS_CHANGE.get(), 1.0F));
                controller.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));

                var newFocusTree = getFocusTree();
                while (!newFocusTree.isEmpty() && !focuses.contains(newFocusTree.peek())) {
                    ComponentProcessorProvider.provide(newFocusTree.poll()).onFocusGained(this, controller);
                }
            }
        }
    }

    protected void handleButtons(ControllerEntity controller) {
        boolean vmouseEnabled = Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled();
        InputComponent input = controller.input().orElseThrow();
        boolean touchpadPressed = input.stateNow().isButtonDown(GamepadInputs.TOUCHPAD_BUTTON);
        boolean prevTouchpadPressed = input.stateThen().isButtonDown(GamepadInputs.TOUCHPAD_BUTTON);

        if (ControlifyBindings.GUI_PRESS.on(controller).guiPressed().get() || (vmouseEnabled && touchpadPressed && !prevTouchpadPressed)) {
            screen.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);
        }
        if (screen.shouldCloseOnEsc() && ControlifyBindings.GUI_BACK.on(controller).guiPressed().get()) {
            playClackSound();
            screen.onClose();
        }
    }

    protected void handleScreenVMouse(ControllerEntity controller, VirtualMouseHandler vmouse) {

    }

    protected boolean handleComponentButtonOverride(ControllerEntity controller) {
        var focusTree = getFocusTree();
        while (!focusTree.isEmpty()) {
            var focused = focusTree.poll();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerButtons(this, controller)) return true;
        }

        return false;
    }

    protected boolean handleComponentNavOverride(ControllerEntity controller) {
        var focusTree = getFocusTree();
        while (!focusTree.isEmpty()) {
            var focused = focusTree.poll();
            var processor = ComponentProcessorProvider.provide(focused);
            if (processor.overrideControllerNavigation(this, controller)) return true;
        }
        return false;
    }

    protected void handleTabNavigation(ControllerEntity controller) {
        var nextTab = ControlifyBindings.GUI_NEXT_TAB.on(controller).justPressed();
        var prevTab = ControlifyBindings.GUI_PREV_TAB.on(controller).justPressed();

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
                        onTabChanged(controller);
                    });
        }
    }

    protected void onTabChanged(ControllerEntity controller) {

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

    protected void render(ControllerEntity controller, GuiGraphics graphics, float tickDelta, Optional<VirtualMouseHandler> vmouse) {

    }

    protected void setInitialFocus() {
        if (screen.getFocused() == null && Controlify.instance().currentInputMode().isController() && !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
            var accessor = (ScreenAccessor) screen;
            ComponentPath path = screen.nextFocusPath(accessor.invokeCreateArrowEvent(ScreenDirection.DOWN));
            if (path != null) {
                accessor.invokeChangeFocus(path);
                holdRepeatHelper.clearDelay();
            }
        }
    }

    public VirtualMouseBehaviour virtualMouseBehaviour() {
        return VirtualMouseBehaviour.DEFAULT;
    }

    public void addEventListener(ScreenControllerEventListener listener) {
        eventListeners.add(listener);
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

    protected final Optional<AbstractWidget> getWidget(Component message) {
        return screen.children().stream()
                .filter(child -> child instanceof AbstractWidget)
                .map(AbstractWidget.class::cast)
                .filter(widget -> widget.getMessage().equals(message))
                .findAny();
    }

    protected final Optional<AbstractWidget> getWidget(String translationKey) {
        String translatedName = Component.translatable(translationKey).getString();

        return screen.children().stream()
                .filter(child -> child instanceof AbstractWidget)
                .map(AbstractWidget.class::cast)
                .filter(widget -> widget.getMessage().getString().equals(translatedName))
                .findAny();
    }

    public static void playClackSound() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
