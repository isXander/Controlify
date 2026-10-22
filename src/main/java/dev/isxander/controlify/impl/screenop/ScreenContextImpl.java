/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.ButtonGuideApi;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.api.screenop.ElementKeyboardBehaviour;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.haptic.HapticEffects;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardOverlayScreen;
import dev.isxander.controlify.impl.sound.ControlifyClientSounds;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

/// One screen's state and dispatch. Default UI behaviour lives in the registered
/// composed screen and element behaviours, rather than in this context.
/// The screen retains this instance for its lifetime.
public final class ScreenContextImpl<T extends Screen> implements ScreenContext<T> {
	private final T screen;
	private final VirtualMouseState virtualMouse;
	private final ElementBehaviourRegistryImpl elements = new ElementBehaviourRegistryImpl();
	private final Map<GuiEventListener, ElementBehaviour> elementCache = new IdentityHashMap<>();
	private final Set<GuiEventListener> constructingElements = identitySet();
	private final ScreenBehaviour behaviour;
	private Set<GuiEventListener> focusedElements = identitySet();
	private boolean elementsReady;
	private boolean widgetsObserved;

	/// Resolves the same context across rebuilds and reopening the same screen.
	@SuppressWarnings("unchecked")
	public static <T extends Screen> ScreenContextImpl<T> get(T screen) {
		return (ScreenContextImpl<T>) ((ScreenContextHolder) screen).controlify$screenContext();
	}

	public ScreenContextImpl(T screen) {
		this.screen = Objects.requireNonNull(screen);
		this.virtualMouse = new VirtualMouseState(this);
		this.behaviour = ScreenBehaviours.chain(ScreenBehaviourRegistryImpl.INSTANCE.create(this));
		try {
			ScreenBehaviourRegistryImpl.Events.REGISTER_ELEMENT_BEHAVIOURS.emit(
					new ScreenopEvents.RegisterElementBehavioursEvent(this, elements));
		} finally {
			elements.lock();
		}
		elementsReady = true;
	}

	@Override
	public T screen() {
		return screen;
	}

	@Override
	public ElementBehaviour elementBehaviour(GuiEventListener element) {
		Objects.requireNonNull(element);
		if (!elementsReady) {
			throw new IllegalStateException("Element behaviours are available after REGISTER_ELEMENT_BEHAVIOURS completes");
		}
		var cached = elementCache.get(element);
		if (cached != null) {
			return cached;
		}
		if (!constructingElements.add(element)) {
			throw new IllegalStateException("Element behaviour factory recursively resolved its own element");
		}
		try {
			var chain = ElementBehaviours.chain(elements.create(element));
			elementCache.put(element, chain);
			return chain;
		} finally {
			constructingElements.remove(element);
		}
	}

	@Override
	public InputMode inputMode() {
		return Controlify.instance().currentInputMode();
	}

	@Override
	public VirtualMouseState virtualMouse() {
		return virtualMouse;
	}

	@Override
	public List<GuiEventListener> focusPath() {
		List<GuiEventListener> path = new ArrayList<>();
		Set<GuiEventListener> visited = identitySet();
		GuiEventListener focused = screen.getFocused();
		while (focused != null && visited.add(focused)) {
			path.addFirst(focused);
			focused = focused instanceof ContainerEventHandler container ? container.getFocused() : null;
		}
		return List.copyOf(path);
	}

	@Override
	public void clearFocus() {
		((ScreenAccessor) screen).controlify$invokeClearFocus();
		synchronizeFocus();
	}

	@Override
	public void setInitialFocus() {
		((ScreenAccessor) screen).invokeSetInitialFocus();
		synchronizeFocus();
	}

	@Override
	public void notifyNavigation(Controller controller) {
		if (controller instanceof ControllerImpl impl) {
			impl.input().onGuiNavigate();
		}
		if (Controlify.instance().config().getSettings().globalSettings().extraUiSounds) {
			playFocusChangeSound();
		}
		playNavigationHaptic(controller);
	}

	@Override
	public void playNavigationHaptic(Controller controller) {
		if (controller instanceof ControllerImpl impl) {
			impl.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));
		}
	}

	@Override
	public void playFocusChangeSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ControlifyClientSounds.SCREEN_FOCUS_CHANGE.get(), 1f));
	}

	@Override
	public void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
	}

	@Override
	public ButtonGuideApi buttonGuide() {
		return dev.isxander.controlify.impl.gui.ButtonGuideApiImpl.INSTANCE;
	}

	public void changeFocus(ComponentPath path) {
		((ScreenAccessor) screen).controlify$invokeChangeFocus(path);
		synchronizeFocus();
	}

	@Override
	public boolean tryOpenKeyboard(@NonNull Controller controller, @Nullable GuiEventListener element) {
		if (element == null || element instanceof AbstractWidget widget && (!widget.active || !widget.visible)) {
			return false;
		}
		var keyboard = elementBehaviour(element).keyboardBehaviour(this);
		if (keyboard.isEmpty()) {
			return false;
		}
		switch (keyboard.get()) {
			case ElementKeyboardBehaviour.DoNothing() -> {
			}
			case ElementKeyboardBehaviour.Handled(var layout, var target, var positioner) -> {
				if (((ControllerImpl) controller).settings().generic.keyboard.showOnScreenKeyboard) {
					MinecraftUtil.setScreen(new KeyboardOverlayScreen(screen, layout, target, positioner));
				}
			}
		}
		return true;
	}

	/// Entry point for the host's fixed update.
	public void handleControllerInput(Controller controller) {
		if (!isCurrentScreen()) {
			return;
		}
		observeWidgets();
		pruneElements();
		synchronizeFocus();
		behaviour.handleNavigation(controller, () -> {
		});
		if (isCurrentScreen()) {
			synchronizeFocus();
			behaviour.handleInteraction(controller, () -> {
			});
			synchronizeFocus();
		}
	}

	public void dispatchElements(Controller controller, boolean navigation, Runnable next) {
		List<ElementBehaviour> path = focusPath().stream().map(this::elementBehaviour).toList();
		BehaviourChain.run(path, (element, continuation) -> {
			if (isCurrentScreen()) {
				if (navigation) {
					element.handleNavigation(this, controller, continuation);
				} else {
					element.handleInteraction(this, controller, continuation);
				}
			}
		}, next);
	}

	public void onWidgetRebuild() {
		widgetsObserved = true;
		pruneElements();
		behaviour.onWidgetRebuild(() -> {
		});
		synchronizeFocus();
	}

	public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller controller) {
		observeWidgets();
		behaviour.extractRenderState(graphics, tickDelta, controller, () -> {
		});
		synchronizeFocus();
	}

	public Set<SnapPoint> collectSnapPoints() {
		Set<SnapPoint> points = new LinkedHashSet<>();
		behaviour.collectSnapPoints(points::add, () -> {
		});
		return Collections.unmodifiableSet(points);
	}

	public void update(float delta) {
		observeWidgets();
		behaviour.update(delta, () -> {
		});
		synchronizeFocus();
	}

	public void onScreenActiveChanged(boolean active) {
		behaviour.onScreenActiveChanged(active);
		if (active) {
			observeWidgets();
		}
		synchronizeFocus();
	}

	private void observeWidgets() {
		if (!widgetsObserved) {
			onWidgetRebuild();
		}
	}

	public void synchronizeFocus() {
		if (!elementsReady) {
			return;
		}
		Set<GuiEventListener> previous = focusedElements;
		Set<GuiEventListener> current = identitySet();
		current.addAll(focusPath());
		focusedElements = current;
		for (GuiEventListener element : previous) {
			if (!current.contains(element)) {
				elementBehaviour(element).onFocusChanged(this, false);
			}
		}
		for (GuiEventListener element : current) {
			if (!previous.contains(element)) {
				elementBehaviour(element).onFocusChanged(this, true);
			}
		}
	}

	private void pruneElements() {
		Set<GuiEventListener> attached = identitySet();
		ArrayDeque<GuiEventListener> remaining = new ArrayDeque<>(screen.children());
		while (!remaining.isEmpty()) {
			GuiEventListener element = remaining.removeFirst();
			if (attached.add(element) && element instanceof ContainerEventHandler container) {
				remaining.addAll(container.children());
			}
		}
		attached.addAll(focusPath());
		// Notify lost focus while the old sidecar is still cached.
		synchronizeFocus();
		elementCache.keySet().removeIf(element -> !attached.contains(element));
	}

	public boolean isCurrentScreen() {
		return MinecraftUtil.getScreen() == screen;
	}

	private static <E> Set<E> identitySet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}
}
