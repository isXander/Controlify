/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.action.ActionLayerRegistryImpl;
import dev.isxander.controlify.impl.action.ActionSpecRegistryImpl;
import dev.isxander.controlify.impl.action.proxy.ActionProxyManagerImpl;

public final class ActionEvents {
	/// Fires before REGISTER_ACTIONS, with built-in layers already registered.
	/// Register addon layer IDs and predicates here; the layer registry locks afterwards.
	public static final EventSource<ActionLayerRegistry> REGISTER_LAYERS = ActionLayerRegistryImpl.Events.REGISTER_LAYERS;

	/// Fires once with Minecraft and initial resources available, before backend setup
	/// or controller construction. Register definitions here; the registry locks afterwards.
	/// Returned accessors may be retained and used with connected controllers.
	public static final EventSource<ActionSpecRegistry> REGISTER_ACTIONS = ActionSpecRegistryImpl.Events.REGISTER_ACTIONS;

	/// Fires immediately after REGISTER_ACTIONS, before automatic modded-key actions or controllers are created.
	/// Configure proxies and automatic-proxy exclusions here; the manager locks after startup registration.
	public static final EventSource<ActionProxyManager> REGISTER_PROXIES = ActionProxyManagerImpl.Events.REGISTER_PROXIES;

	private ActionEvents() {
	}
}
