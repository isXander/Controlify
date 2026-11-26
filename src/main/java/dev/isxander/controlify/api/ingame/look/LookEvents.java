/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.ingame.look;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.ingame.look.LookRegistryImpl;

public final class LookEvents {
	/// Fires once during early setup, after Controlify's built-in sources are registered.
	/// Subscribe in the Controlify entrypoint. The registry locks afterwards.
	public static final EventSource<LookRegistry> REGISTER = LookRegistryImpl.Events.REGISTER;

	private LookEvents() {
	}
}
