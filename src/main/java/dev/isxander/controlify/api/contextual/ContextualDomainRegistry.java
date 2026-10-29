/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import net.minecraft.resources.Identifier;

/// Supplied during ContextualEvents.REGISTER_DOMAINS for registering custom domains
/// and accessing built-in domains, for example to register contributors.
public interface ContextualDomainRegistry {
	/// Register during ContextualEvents.REGISTER_DOMAINS, before initial resource loading.
	/// Throws when registration is closed or the domain identifier is already registered.
	<C extends Context> ContextualDomain<C> register(Identifier domainId);

	<C extends Context> ContextualDomain<C> register(Identifier domainId, ContextualStateContributor<? super C> contributor);
}
