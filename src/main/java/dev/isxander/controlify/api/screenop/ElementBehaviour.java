/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.controller.Controller;

import java.util.Optional;
import java.util.function.Consumer;

/// Controller support for one GUI element within one screen context.
/// Registered [factories][ElementBehaviourFactory] create stateful instances lazily;
/// [ScreenContext#elementBehaviour] resolves the composed chain by element identity.
///
/// Methods with `next` use synchronous, at-most-once continuations. Omitting
/// `next` stops the remaining chain for that invocation, including outer focused
/// elements and remaining screen controls when called by built-in input routing. It does
/// not consume action pulses or cancel another stage. Never retain a continuation.
///
/// Navigation runs before interaction. Each stage resolves its own focus path from
/// deepest to outermost, so interaction can target an element newly focused by navigation.
/// Focus notifications are broadcast independently of input cancellation; keyboard policies
/// are resolved by the first non-empty result rather than by a continuation.
public interface ElementBehaviour {
	/// Handles directional input before the screen's interaction stage.
	/// The built-in dispatcher invokes the deepest focused element first, then its focused
	/// ancestors, then the remaining screen navigation. Call `next` to allow that
	/// routing, or omit it after handling navigation, such as adjusting an editing slider.
	///
	/// Stopping navigation does not stop interaction. Focus is synchronized after the
	/// navigation stage, and interaction resolves the resulting path afresh. Pulses for
	/// unconsumed actions remain subject to the action system's own lifetime rules.
	///
	/// @param screen the screen context owning this element behaviour
	/// @param controller the controller whose actions are being processed
	/// @param next continues element and screen navigation synchronously, at most once
	default void handleNavigation(ScreenContext<?> screen, Controller controller, Runnable next) {
		next.run();
	}

	/// Handles activation, Back, or other actions after navigation and focus synchronization.
	/// With built-in routing, the deepest currently focused element runs before its ancestors
	/// and the remaining screen interaction behaviours.
	///
	/// Omit `next` to handle this interaction exclusively. This does not consume
	/// unrelated pulses or prevent future input calls; drain conflicting actions explicitly
	/// when they must also be suppressed.
	///
	/// @param screen the screen context owning this element behaviour
	/// @param controller the controller whose actions are being processed
	/// @param next continues element and screen interaction synchronously, at most once
	default void handleInteraction(ScreenContext<?> screen, Controller controller, Runnable next) {
		next.run();
	}

	/// Notifies this behaviour when its element enters or leaves the screen's focus path.
	/// Focused ancestors receive notifications as well as the deepest element. Changes made
	/// by other elements or by the screen are included when the context synchronizes focus.
	/// The order of notifications between different elements is unspecified.
	///
	/// This is a broadcast lifecycle notification: every behaviour in the element's
	/// composed chain receives it, regardless of previous input cancellation.
	/// [ElementBehaviours#conditional] and [ElementBehaviours#enabledWidget]
	/// forward it even while their conditions are false. Use it to reset editing or transient
	/// state; there is no continuation to call or suppress. Screen activation changes are
	/// separate and do not themselves imply that the focus path has changed.
	///
	/// @param screen the screen context owning this element behaviour
	/// @param focused whether this element is now in that context's focus path
	default void onFocusChanged(ScreenContext<?> screen, boolean focused) {
	}

	/// Supplies this element's policy for on-screen keyboard activation.
	/// The first non-empty result in the composed chain wins. An empty result lets later
	/// behaviours supply a policy; [ElementKeyboardBehaviour.DoNothing] explicitly
	/// handles activation without opening a popup, for example when the screen embeds a keyboard.
	///
	/// This query has no continuation and is independent of input cancellation. Conditional
	/// wrappers return empty while disabled. Returning a policy does not itself open a keyboard;
	/// [ScreenContext#tryOpenKeyboard] performs that operation.
	///
	/// @param screen the screen context owning this element behaviour
	/// @return a keyboard policy, or empty to inherit a later behaviour's policy
	default Optional<ElementKeyboardBehaviour> keyboardBehaviour(ScreenContext<?> screen) {
		return Optional.empty();
	}

	/// Contributes this element's virtual mouse targets in scaled GUI coordinates.
	/// The built-in remaining chain supplies widget centres or child traversal for containers.
	/// Omit `next` to replace those contributions, including the descendants of a
	/// container. This collection follows the element hierarchy, independently of focus.
	///
	/// Read current layout geometry on each call and use `output` only during
	/// this invocation.
	///
	/// @param screen the screen context owning this element behaviour
	/// @param output receives snap points contributed by this element
	/// @param next continues element snap point collection synchronously, at most once
	default void collectSnapPoints(ScreenContext<?> screen, Consumer<SnapPoint> output, Runnable next) {
		next.run();
	}
}
