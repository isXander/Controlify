/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.contextual.ContextualDomains;

public final class ContextualEvents {
	/// Fires once during early setup, before initial resource loading. Register domains
	/// here so their resource listeners join the first reload. New-domain registration
	/// closes afterwards; the registry and existing domains may be retained.
	public static final EventSource<ContextualDomainRegistry> REGISTER_DOMAINS = ContextualDomains.Events.REGISTER_DOMAINS;

	private ContextualEvents() {}
}
