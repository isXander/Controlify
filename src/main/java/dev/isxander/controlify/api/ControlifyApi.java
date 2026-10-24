/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api;

import dev.isxander.controlify.api.contextual.BuiltinContextualDomains;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import dev.isxander.controlify.impl.Controlify;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Optional;

/// Live client queries and operations. Access through {@link #get()} or event payloads;
/// consumers do not need to retain the API or reconstruct state from notifications.
public interface ControlifyApi {
	/// Returns the shared client API without initializing its subsystems. Action-spec and
	/// contextual-domain registries must still be obtained through their registration events; INITIALIZED
	/// indicates when runtime managers are ready, rather than granting access to this API.
	static ControlifyApi get() {
		return Controlify.instance();
	}

	Optional<? extends Controller> getCurrentController();

	/// An unmodifiable snapshot of the currently connected controllers.
	Collection<? extends Controller> getConnectedControllers();

	Optional<? extends Controller> getControllerByUid(String uid);

	/// Access to built-in contextual domains throughout the client lifetime, without
	/// exposing the new-domain registration operations supplied during REGISTER_DOMAINS.
	BuiltinContextualDomains contextualDomains();

	InputMode currentInputMode();

	/// Returns whether the mode changed. Controller modes require a current controller.
	boolean setInputMode(InputMode mode);

	default Optional<RumbleEffectInstance> playRumbleEffect(Identifier effectId, RumbleEffect effect) {
		return getCurrentController().flatMap(Controller::rumble).map(rumble -> rumble.playEffect(effectId, effect));
	}
}
