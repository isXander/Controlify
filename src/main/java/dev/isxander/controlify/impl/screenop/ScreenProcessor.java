/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.haptic.HapticEffects;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import dev.isxander.controlify.impl.input.mapping.GamepadInputs;
import dev.isxander.controlify.impl.screenop.keyboard.*;
import dev.isxander.controlify.impl.sound.ControlifyClientSounds;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.impl.virtualmouse.VirtualMouseBehaviour;
import dev.isxander.controlify.impl.virtualmouse.VirtualMouseHandler;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.TabNavigationBarAccessor;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class ScreenProcessor<T extends Screen> {
	public final T screen;
	protected static final Minecraft minecraft = Minecraft.getInstance();

	private final List<ScreenControllerEventListener> eventListeners = new ArrayList<>();

	public ScreenProcessor(T screen) {
		this.screen = screen;
		if (screen instanceof ScreenControllerEventListener eventListener) {
			eventListeners.add(eventListener);
		}
	}

	public void onControllerFixedUpdate(ControllerImpl controller) {
		Controlify.instance().virtualMouseHandler().handleControllerInput(controller);

		if (!Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
			if (!handleComponentNavOverride(controller)) {
				handleComponentNavigation(controller);
			}

			if (!handleComponentButtonOverride(controller)) {
				handleButtons(controller);
			}
		} else {
			handleScreenVMouse(controller, Controlify.instance().virtualMouseHandler());
		}

		handleTabNavigation(controller);

		eventListeners.forEach(listener -> listener.onControllerInput(controller));
	}

	public void render(ControllerImpl controller, GuiGraphicsExtractor graphics, float tickDelta) {
		var vmouse = Controlify.instance().virtualMouseHandler();
		this.render(controller, graphics, tickDelta, vmouse.isVirtualMouseEnabled() ? Optional.of(vmouse) : Optional.empty());
	}

	public void onInputModeChanged(InputMode mode) {
		switch (mode) {
			case KEYBOARD_MOUSE -> {
				boolean shouldKeepFocus = getFocusTree().stream()
						.anyMatch(component -> ComponentProcessorProvider.provide(component).shouldKeepFocusOnKeyboardMode(this));

				if (!shouldKeepFocus) {
					((ScreenAccessor) screen).controlify$invokeClearFocus();
				}
			}
			case CONTROLLER, MIXED -> {
				if (!Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
					((ScreenAccessor) screen).invokeSetInitialFocus();
				}
			}
		}
	}

	protected void handleComponentNavigation(ControllerImpl controller) {
		if (screen.getFocused() == null) {
			((ScreenAccessor) screen).invokeSetInitialFocus();
		}

		var focuses = List.copyOf(getFocusTree());

		Action guiNaviRight = ControlifyActions.GUI_NAVI_RIGHT.on(controller);
		Action guiNaviLeft = ControlifyActions.GUI_NAVI_LEFT.on(controller);
		Action guiNaviUp = ControlifyActions.GUI_NAVI_UP.on(controller);
		Action guiNaviDown = ControlifyActions.GUI_NAVI_DOWN.on(controller);

		Supplier<Boolean> navigationFunc = null;
		if (guiNaviRight.pulse().consumePulse()) {
			navigationFunc = this.createScreenNavigationFunc(ScreenDirection.RIGHT);
		} else if (guiNaviLeft.pulse().consumePulse()) {
			navigationFunc = this.createScreenNavigationFunc(ScreenDirection.LEFT);
		} else if (guiNaviUp.pulse().consumePulse()) {
			navigationFunc = this.createScreenNavigationFunc(ScreenDirection.UP);
		} else if (guiNaviDown.pulse().consumePulse()) {
			navigationFunc = this.createScreenNavigationFunc(ScreenDirection.DOWN);
		}

		if (navigationFunc != null) {
			// ScrollableLayout only reveals newly focused children for keyboard-style navigation.
			// Set this at the point of navigation as the controller input mode may not have been
			// established yet when a newly opened screen built its widgets.
			minecraft.setLastInputType(InputType.KEYBOARD_ARROW);
			if (navigationFunc.get()) {

				controller.input().onGuiNavigate();

				if (Controlify.instance().config().getSettings().globalSettings().extraUiSounds) {
					playFocusChangeSound();
				}
				controller.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));

				var newFocusTree = getFocusTree();
				while (!newFocusTree.isEmpty() && !focuses.contains(newFocusTree.peek())) {
					ComponentProcessorProvider.provide(newFocusTree.poll()).onFocusGained(this, controller);
				}
			}
		}
	}

	protected @Nullable Supplier<Boolean> createScreenNavigationFunc(ScreenDirection direction) {
		var event = new FocusNavigationEvent.ArrowNavigation(direction);
		var path = screen.nextFocusPath(event);
		if (path == null) {
			return null;
		}

		return () -> {
			((ScreenAccessor) screen).controlify$invokeChangeFocus(path);
			return true;
		};
	}

	protected void handleButtons(ControllerImpl controller) {
		boolean vmouseEnabled = Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled();
		InputComponentImpl input = controller.input();
//		boolean touchpadPressed = input.stateNow().isButtonDown(GamepadInputs.TOUCHPAD_1_BUTTON);
//		boolean prevTouchpadPressed = input.stateThen().isButtonDown(GamepadInputs.TOUCHPAD_1_BUTTON);
		boolean touchpadPressed = false;
		boolean prevTouchpadPressed = false;

		if (ControlifyActions.GUI_PRESS.on(controller).pulse().consumePulse() || (vmouseEnabled && touchpadPressed && !prevTouchpadPressed)) {
			if (!this.tryOpenKeyboard(controller, screen.getFocused())) {
				screen.keyPressed(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
			}
		}
		if (screen.shouldCloseOnEsc() && ControlifyActions.GUI_BACK.on(controller).pulse().consumePulse()) {
			playClackSound();
			screen.onClose();
		}
	}

	protected void handleScreenVMouse(ControllerImpl controller, VirtualMouseHandler vmouse) {
		Minecraft.getInstance().setLastInputType(InputType.MOUSE);
	}

	protected boolean handleComponentButtonOverride(ControllerImpl controller) {
		var focusTree = getFocusTree();
		while (!focusTree.isEmpty()) {
			var focused = focusTree.poll();
			var processor = ComponentProcessorProvider.provide(focused);
			if (processor.overrideControllerButtons(this, controller)) {
				return true;
			}
		}

		return false;
	}

	protected boolean handleComponentNavOverride(ControllerImpl controller) {
		var focusTree = getFocusTree();
		while (!focusTree.isEmpty()) {
			var focused = focusTree.poll();
			var processor = ComponentProcessorProvider.provide(focused);
			if (processor.overrideControllerNavigation(this, controller)) {
				return true;
			}
		}
		return false;
	}

	protected void handleTabNavigation(ControllerImpl controller) {
		var nextTab = ControlifyActions.GUI_NEXT_TAB.on(controller).pulse().consumePulse();
		var prevTab = ControlifyActions.GUI_PREV_TAB.on(controller).pulse().consumePulse();

		if (nextTab || prevTab) {
			screen.children().stream()
					.filter(child -> child instanceof TabNavigationBar)
					.map(TabNavigationBar.class::cast)
					.findAny()
					.ifPresent(navBar -> {
						var accessor = (TabNavigationBarAccessor) navBar;
						List<Tab> tabs = accessor.controlify$getTabs();
						int currentIndex = tabs.indexOf(accessor.controlify$getTabManager().getCurrentTab());

						int newIndex = currentIndex + (prevTab ? -1 : 1);
						if (newIndex < 0) {
							newIndex = tabs.size() - 1;
						}
						if (newIndex >= tabs.size()) {
							newIndex = 0;
						}

						navBar.selectTab(newIndex, true);
						onTabChanged(controller);
					});
		}
	}

	protected void onTabChanged(ControllerImpl controller) {
	}

	public void onWidgetRebuild() {
		if (Controlify.instance().currentInputMode().isController() && !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
			// setting this input type causes the vanilla screen to call setInitialFocus
			// doing it again would cause the second widget to be focused instead of the first
			minecraft.setLastInputType(InputType.KEYBOARD_ARROW);
		}
	}

	public void onVirtualMouseToggled(boolean enabled) {
		if (enabled) {
			((ScreenAccessor) screen).controlify$invokeClearFocus();
		} else {
			((ScreenAccessor) screen).invokeSetInitialFocus();
		}
	}

	protected void render(ControllerImpl controller, GuiGraphicsExtractor graphics, float tickDelta, Optional<VirtualMouseHandler> vmouse) {
	}

	public VirtualMouseBehaviour virtualMouseBehaviour() {
		return VirtualMouseBehaviour.DEFAULT;
	}

	public void addEventListener(ScreenControllerEventListener listener) {
		eventListeners.add(listener);
	}

	public boolean tryOpenKeyboard(ControllerImpl controller, @Nullable GuiEventListener element) {
		var componentProcessor = ComponentProcessorProvider.provide(element);
		ComponentKeyboardBehaviour behaviour = componentProcessor.getKeyboardBehaviour(this, controller);

		switch (behaviour) {
			case ComponentKeyboardBehaviour.DoNothing() -> {
				// prevent pressing enter on select when handled
				return true;
			}
			case ComponentKeyboardBehaviour.Undefined() -> {
				return false;
			}
			case ComponentKeyboardBehaviour.Handled(
					KeyboardLayoutWithId layout,
					InputTarget inputTarget,
					KeyboardOverlayScreen.KeyboardPositioner positioner
			) -> {
				if (controller.settings().generic.keyboard.showOnScreenKeyboard) {
					MinecraftUtil.setScreen(new KeyboardOverlayScreen(screen, layout, inputTarget, positioner));
				}
				return true;
			}
		}
	}

	protected Queue<GuiEventListener> getFocusTree() {
		if (screen.getFocused() == null) {
			return new ArrayDeque<>();
		}

		var tree = new ArrayDeque<GuiEventListener>();
		var focused = screen.getFocused();
		tree.add(focused);
		while (focused instanceof CustomFocus customFocus) {
			focused = customFocus.getCustomFocus();

			if (focused != null) {
				tree.addFirst(focused);
			}
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

	public static void playFocusChangeSound() {
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ControlifyClientSounds.SCREEN_FOCUS_CHANGE.get(), 1.0F));
	}
}
