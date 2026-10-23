/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import net.minecraft.client.gui.components.events.GuiEventListener;

/// Registers additive element behaviour factories during
/// [ScreenopEvents#REGISTER_ELEMENT_BEHAVIOURS].
/// This registry belongs to one screen context, so the same element type can receive different support on different screens.
/// Matching registrations compose rather than replace one another. Registration is
/// only allowed while the event is being dispatched.
public interface ElementBehaviourRegistry {
	/// Adds a factory for the supplied type and its subtypes at an explicit priority.
	/// Higher priorities run first. Within a priority, more specific assignable types run
	/// before their supertypes; otherwise newer registrations run first. Positive and
	/// negative priorities are supported, and the ordinary default is zero.
	///
	/// Each matching registration contributes a behaviour to the chain. Use
	/// [ElementBehaviours#chain] when a group requires a fixed internal order without
	/// assigning separate priorities to its members.
	///
	/// @param <T> the registered element type
	/// @param elementClass the element class or interface to match, including subtypes
	/// @param priority the ordering priority; higher values run earlier
	/// @param factory creates a behaviour lazily for each matching element in this screen context
	/// @throws IllegalStateException if registration has closed
	<T extends GuiEventListener> void register(Class<T> elementClass, int priority, ElementBehaviourFactory<? super T> factory);

	/// Adds a factory for the supplied type and its subtypes at priority zero.
	/// Other matching factories remain in the chain. Within this priority, more specific
	/// assignable types run first, then newer registrations. Equivalent to calling the
	/// explicit-priority overload with `0`.
	///
	/// @param <T> the registered element type
	/// @param elementClass the element class or interface to match, including subtypes
	/// @param factory creates a behaviour lazily for each matching element in this screen context
	/// @throws IllegalStateException if registration has closed
	default <T extends GuiEventListener> void register(Class<T> elementClass, ElementBehaviourFactory<? super T> factory) {
		register(elementClass, 0, factory);
	}
}
