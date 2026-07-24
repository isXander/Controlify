/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.rumble;

import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.rumble.TriggerRumbleState;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TriggerRumbleComponent extends ECSComponentImpl {
	public static final Identifier ID = CUtil.rl("trigger_rumble");

	private TriggerRumbleState state = null;

	public void queueTriggerRumble(@NotNull TriggerRumbleState state) {
		this.state = state;
	}

	public Optional<TriggerRumbleState> consumeTriggerRumble() {
		TriggerRumbleState state = this.state;
		this.state = null;
		return Optional.ofNullable(state);
	}

	@Override
	public Identifier id() {
		return ID;
	}
}
