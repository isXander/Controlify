/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import net.minecraft.client.gui.screens.Screen;

/// Registers additive screen behaviour factories during
/// [ScreenopEvents#REGISTER_SCREEN_BEHAVIOURS].
/// This registry is global to screen types.
/// Matching registrations compose rather than replace one another. Registration is
/// only allowed while the event is being dispatched.
public interface ScreenBehaviourRegistry {
	/// Adds a factory for the supplied type and its subtypes at an explicit priority.
	/// Higher priorities run first. Within a priority, more specific assignable types run
	/// before their supertypes; otherwise newer registrations run first. Positive and
	/// negative priorities are supported, and the ordinary default is zero.
	///
	/// Each matching registration contributes a behaviour to the chain. Use
	/// [ScreenBehaviours#chain] when a group requires a fixed internal order without
	/// assigning separate priorities to its members.
	///
	/// @param <T> the registered screen type
	/// @param screenClass the screen class to match, including subtypes
	/// @param priority the ordering priority; higher values run earlier
	/// @param factory creates a behaviour once per screen context
	/// @throws IllegalStateException if registration has closed
	<T extends Screen> void register(Class<T> screenClass, int priority, ScreenBehaviourFactory<? super T> factory);

	/// Adds a factory for the supplied type and its subtypes at priority zero.
	/// Other matching factories remain in the chain. Within this priority, more specific
	/// assignable types run first, then newer registrations. Equivalent to calling the
	/// explicit-priority overload with `0`.
	///
	/// @param <T> the registered screen type
	/// @param screenClass the screen class to match, including subtypes
	/// @param factory creates a behaviour once per screen context
	/// @throws IllegalStateException if registration has closed
	default <T extends Screen> void register(Class<T> screenClass, ScreenBehaviourFactory<? super T> factory) {
		register(screenClass, 0, factory);
	}
}
