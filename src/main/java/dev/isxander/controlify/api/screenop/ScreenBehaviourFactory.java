/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import net.minecraft.client.gui.screens.Screen;

/// Constructs a screen sidecar for an explicitly registered screen type.
/// Each matching registration runs once when a screen context is created. Return a fresh
/// stateful behaviour, or compose several with [ScreenBehaviours]; do not recreate
/// stateful behaviours on every input callback.
///
/// @param <T> the screen type accepted by this factory
@FunctionalInterface
public interface ScreenBehaviourFactory<T extends Screen> {
	/// Creates a behaviour for the supplied screen context.
	/// The screen is constructed, but its widgets may not yet be initialized. Per-screen
	/// element registration has not finished, so defer calls to
	/// [ScreenContext#elementBehaviour] until after factory construction and registration.
	///
	/// @param screen the context shared by all behaviours attached to this screen
	/// @return the non-null sidecar to retain for this context
	ScreenBehaviour create(ScreenContext<? extends T> screen);
}
