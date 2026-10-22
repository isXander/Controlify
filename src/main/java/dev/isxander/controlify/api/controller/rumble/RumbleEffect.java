/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.controller.rumble;

import dev.isxander.controlify.impl.controller.rumble.FiniteRumbleEffectImpl;
import dev.isxander.controlify.impl.controller.rumble.ContinuousRumbleEffectImpl;

import java.util.function.BooleanSupplier;

/// A reusable effect definition. Configuration methods return new definitions;
/// playback state is held by the instance returned from RumbleComponent.playEffect.
/// Callbacks may be shared by multiple playbacks and are invoked on the client thread.
public sealed interface RumbleEffect permits FiniteRumbleEffect, ContinuousRumbleEffect {
	/// Samples this definition at a zero-based playback tick.
	RumbleState stateAt(int tick);

	/// Returns whether playback should end before its next sample.
	boolean shouldStop();

	/// Returns a copy with an additional stop condition. Conditions are ORed.
	RumbleEffect stopCondition(BooleanSupplier condition);

	static FiniteRumbleEffect finite() {
		return new FiniteRumbleEffectImpl();
	}

	static ContinuousRumbleEffect continuous() {
		return new ContinuousRumbleEffectImpl();
	}
}
