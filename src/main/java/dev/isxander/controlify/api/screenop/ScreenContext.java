/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.ButtonGuideApi;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Shared state and services for behaviours attached to one screen instance.
/// The context survives widget rebuilds and returning to the same screen after an overlay.
/// Screen factories run before per-screen element registration, so element behaviours
/// cannot be resolved until that registration event finishes. Widgets may not yet exist
/// when a screen factory runs; resolve rebuilt widgets in
/// [ScreenBehaviour#onWidgetRebuild] instead.
///
/// @param <T> the screen type exposed to this behaviour
public interface ScreenContext<T extends Screen> {
	/// Returns the screen owning this context.
	///
	/// @return the same screen instance throughout this context's lifetime
	T screen();

	/// Resolves the effective behaviour chain for an element in this screen.
	/// Matching factories run lazily and the result is cached by object identity. Repeated
	/// lookups return the cached chain while that element remains retained by the context;
	/// replaced or detached widgets can be discarded during hierarchy synchronization.
	/// The same element used in another screen context has an independent chain.
	///
	/// Built-in routing performs ordinary input and snap point lookups. Use this method
	/// when a custom behaviour needs to dispatch to a child or query its keyboard policy.
	///
	/// @param element the element whose composed behaviour is needed
	/// @return the chain of all matching element behaviours, possibly empty
	/// @throws IllegalStateException if element registration has not finished, or a factory
	///   recursively attempts to resolve its own element
	ElementBehaviour elementBehaviour(GuiEventListener element);

	/// Returns the current input mode, evaluated when this method is called.
	///
	/// @return the client's current input mode
	InputMode inputMode();

	/// Returns this screen's shared virtual mouse.
	/// Use it to configure cursor policy or perform explicit snapping and click operations.
	///
	/// @return the cursor instance retained by this context
	VirtualMouse virtualMouse();

	/// Reports the effective enabled state of this screen's virtual mouse.
	/// Equivalent to `virtualMouse().isEnabled()`, including activation and input mode
	/// requirements rather than checking the configured mode alone.
	///
	/// @return whether this screen's cursor is currently active
	default boolean isVirtualMouseEnabled() {
		return virtualMouse().isEnabled();
	}

	/// Captures the screen's current focus path, deepest element first, followed by its
	/// focused ancestors. The screen itself is excluded. This is an immutable snapshot;
	/// request a new path after changing focus.
	///
	/// @return the current focused elements, or an empty list if nothing is focused
	List<GuiEventListener> focusPath();

	/// Clears the screen's focus and synchronizes element focus notifications immediately.
	/// Behaviours whose elements leave the focus path receive
	/// [ElementBehaviour#onFocusChanged] with `false`.
	void clearFocus();

	/// Asks the screen to establish its initial focus and synchronizes element notifications.
	/// Uses the screen's normal initial-focus logic, including screen-specific overrides.
	void setInitialFocus();

	/// Reports a successful navigation step and supplies standard feedback.
	/// Notifies the controller input system, plays navigation haptics when available, and
	/// plays the focus-change sound when the extra UI sounds setting is enabled.
	///
	/// @param controller the controller responsible for the navigation
	void notifyNavigation(Controller controller);

	/// Plays the standard navigation haptic if supported by the controller.
	/// This does not report a navigation step or play a sound.
	///
	/// @param controller the controller receiving haptic feedback
	void playNavigationHaptic(Controller controller);

	/// Plays the standard focus-change sound directly, regardless of the extra UI sounds
	/// setting. Use [#notifyNavigation] for navigation feedback that respects that setting.
	void playFocusChangeSound();

	/// Plays the standard GUI button click sound without reporting navigation or playing haptics.
	void playClickSound();

	/// Returns the button guide service for adding controller action hints to widgets.
	/// Attach guides to newly created widgets during [ScreenBehaviour#onWidgetRebuild].
	///
	/// @return the button guide API
	ButtonGuideApi buttonGuide();

	/// Resolves an element's keyboard policy and handles activation if one is supplied.
	/// A [ElementKeyboardBehaviour.Handled] policy opens a popup when the user's
	/// on-screen keyboard setting permits it. A [ElementKeyboardBehaviour.DoNothing]
	/// policy suppresses the popup. Both count as handled; a supplied policy also counts as
	/// handled when the user has disabled the popup keyboard.
	///
	/// A null element, an inactive or invisible widget, or an absent policy returns
	/// `false`, allowing ordinary activation to continue. This method does not itself
	/// consume activation pulses.
	///
	/// @param controller the controller whose keyboard preference is used
	/// @param component the element to query, or null when nothing is targeted
	/// @return whether activation was handled, including intentional popup suppression
	boolean tryOpenKeyboard(
			Controller controller,
			@Nullable GuiEventListener component
	);
}
