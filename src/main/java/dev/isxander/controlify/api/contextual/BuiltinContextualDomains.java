/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import dev.isxander.controlify.impl.contextual.ContextualDomains;

/// Access to Controlify's built-in domains throughout runtime
public final class BuiltinContextualDomains {

	public static final ContextualDomain<InGameContext> IN_GAME = ContextualDomains.INSTANCE.inGame();

	public static final ContextualDomain<ContainerContext> CONTAINER = ContextualDomains.INSTANCE.container();
}
