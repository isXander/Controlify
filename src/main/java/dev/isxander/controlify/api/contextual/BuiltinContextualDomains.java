/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

/// Access to Controlify's built-in domains during registration and throughout runtime.
public interface BuiltinContextualDomains {
	ContextualDomain<InGameContext> inGame();

	ContextualDomain<ContainerContext> container();
}
