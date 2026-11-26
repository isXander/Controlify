/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import net.minecraft.client.gui.components.events.GuiEventListener;

/// Constructs an element sidecar within a particular screen context.
/// Factories are explicitly registered during the per-screen element registration event
/// and may capture that event's [ScreenContext]. Separate registrations compose;
/// return a fresh stateful behaviour for each element rather than sharing mutable state
/// between elements or screens.
///
/// @param <T> the element type accepted by this factory
@FunctionalInterface
public interface ElementBehaviourFactory<T extends GuiEventListener> {
	/// Creates a behaviour when this element is first resolved in the owning context.
	/// The result is cached by element identity while retained by that context. Do not
	/// recursively resolve the same element through [ScreenContext#elementBehaviour]
	/// from this method; compose the needed behaviours directly instead.
	///
	/// @param element the GUI element receiving controller support
	/// @return the non-null sidecar to retain for this element
	ElementBehaviour create(T element);
}
