/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.rumble;

import dev.isxander.controlify.api.controller.rumble.RumbleComponent;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.config.settings.profile.RumbleSettings;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class RumbleComponentImpl extends ControllerComponent implements RumbleComponent {
	public static final Identifier ID = CUtil.rl("rumble");

	private RumbleState state = null;
	private final RumbleManager rumbleManager;

	public RumbleComponentImpl() {
		super(ID);
		this.rumbleManager = new RumbleManager(this);
	}

	@Override
	public @NonNull RumbleEffectInstance playEffect(@NonNull Identifier effectId, @NonNull RumbleEffect effect) {
		return this.rumbleManager.play(effectId, effect);
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

	public RumbleManager rumbleManager() {
		return this.rumbleManager;
	}

	public RumbleSettings settings() {
		return this.controller().settings().rumble;
	}

	public RumbleSettings defaultSettings() {
		return this.controller().defaultSettings().rumble;
	}

}
