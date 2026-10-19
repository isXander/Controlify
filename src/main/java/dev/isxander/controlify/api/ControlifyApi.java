/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Interface with Controlify in a manner where you don't need to worry about updates
 * breaking! This is the recommended way to interact with Controlify.
 * <p>
 * Alternatively, to use Controlify directly, you can use {@link Controlify#instance()}. Though
 * beware, things may break at any time!
 * <p>
 * Anything that is asked for from this API is safe to use, even if it is not in the API package.
 */
public interface ControlifyApi {
	/**
	 * The controller that is currently enabled and in use.
	 * If there is no controller disconnected or disabled, this will return {@link Optional#empty()}.
	 * This is the controller that is used for {@link ControlifyEvents#ACTIVE_CONTROLLER_TICKED}
	 */
	Optional<Controller> getCurrentController();

	/**
	 * The last input received: a controller or keyboard/mouse.
	 */
	InputMode currentInputMode();
	boolean setInputMode(InputMode mode);

	default Optional<RumbleEffectInstance> playRumbleEffect(Identifier effectId, RumbleEffect rumbleEffect) {
		return getCurrentController()
				.flatMap(Controller::rumble)
				.map(r -> r.playEffect(effectId, rumbleEffect));
	}

	static ControlifyApi get() {
		return Controlify.instance();
	}
}
