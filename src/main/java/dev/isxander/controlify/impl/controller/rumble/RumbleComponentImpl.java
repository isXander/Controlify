/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.rumble;

import dev.isxander.controlify.impl.config.settings.profile.RumbleSettings;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/// The two rumble motors of a controller. Written to by the feedback engine's
/// `DualMotorRenderer` and read by the driver.
public class RumbleComponentImpl extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("rumble");

	private RumbleState state = null;

	public RumbleComponentImpl() {
		super(ID);
	}

	public void queueRumble(RumbleState state) {
		if (settings().enabled || state.isZero()) {
			this.state = state;
		}
	}

	public Optional<RumbleState> consumeRumble() {
		RumbleState state = this.state;
		this.state = null;
		return Optional.ofNullable(state);
	}

	public RumbleSettings settings() {
		return this.controller().settings().rumble;
	}

	public RumbleSettings defaultSettings() {
		return this.controller().defaultSettings().rumble;
	}
}
