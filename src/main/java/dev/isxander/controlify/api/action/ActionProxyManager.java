/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.api.controller.Controller;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

/// Configures how existing actions proxy operations in Minecraft.
/// Supplied by {@link ActionEvents#REGISTER_PROXIES}, immediately after action-spec registration.
/// Configure proxies and automatic-proxy exclusions in that event; registrations close after startup setup.
public interface ActionProxyManager {
	/// Prevents automatic creation of an action for this key mapping.
	/// Does not prevent explicitly proxying it through an existing action.
	/// Useless for vanilla key mappings as they are not automatically proxied.
	void dontAutoProxyKeyMapping(KeyMapping keyMapping);

	/// Proxies an already registered action to a key mapping, suppressing its automatic proxy action.
	/// The listener consumes pulses to generate key clicks and reads the latch for held state.
	/// It resolves the action again on each update so profile changes can replace action instances.
	/// A key mapping may only be proxied by one action; an action may proxy multiple keys.
	default void proxyKeyMapping(ActionAccessor action, KeyMapping keyMapping) {
		proxyKeyMapping(action.actionId(), keyMapping);
	}

	/// Equivalent to {@link #proxyKeyMapping(ActionAccessor, KeyMapping)} using a registered action ID.
	/// Throws if the action is not registered or the key is already proxied by a different action.
	default void proxyKeyMapping(Identifier actionId, KeyMapping keyMapping) {
		proxyKeyMapping(actionId, keyMapping, controller -> false);
	}

	/// Proxies an action with a controller-dependent toggle setting.
	/// When the predicate is true, each pulse toggles the key's held state; otherwise the latch supplies it.
	/// Toggle state clears when the action resets or the controller stops owning the proxy.
	/// The action must produce pulses for toggle mode and a latch for hold mode.
	default void proxyKeyMapping(ActionAccessor action, KeyMapping keyMapping, Predicate<Controller> toggleCondition) {
		proxyKeyMapping(action.actionId(), keyMapping, toggleCondition);
	}

	/// Equivalent to {@link #proxyKeyMapping(ActionAccessor, KeyMapping, Predicate)} using an action ID.
	void proxyKeyMapping(Identifier actionId, KeyMapping keyMapping, Predicate<Controller> toggleCondition);
}
