/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

/// Receives action definitions during {@link ActionEvents#REGISTER_ACTIONS}.
public interface ActionSpecRegistry {
	/// Throws if registration has closed or the identifier is already registered.
	ActionAccessor register(ActionSpec actionSpec);
}
