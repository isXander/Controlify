/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

/// Supplied during ContextualEvents.REGISTER_DOMAINS for registering custom domains
/// and accessing built-in domains, for example to register contributors.
@ApiStatus.NonExtendable
public interface ContextualDomainRegistry {
	/// Register during ContextualEvents.REGISTER_DOMAINS, before initial resource loading.
	/// Throws when registration is closed or the domain identifier is already registered.
	<C extends Context> ContextualDomain<C> register(Identifier domainId);

	/// Register during ContextualEvents.REGISTER_DOMAINS, before initial resource loading.
	/// Throws when registration is closed or the domain identifier is already registered.
	///
	/// Accepts an initial state contributor equivalent to
	/// ```java
	/// var domain = this.register(domaianId);
	/// domain.registerContributor(contributor);
	/// ```
	<C extends Context> ContextualDomain<C> register(Identifier domainId, ContextualStateContributor<? super C> contributor);
}
