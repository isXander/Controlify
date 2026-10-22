/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.controller.rumble;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntFunction;

/// An immutable, finite effect definition. Each configuration method returns a copy.
public non-sealed interface FiniteRumbleEffect extends RumbleEffect {
	@Override
	FiniteRumbleEffect stopCondition(BooleanSupplier condition);

	int durationTicks();

	/// Replaces the waveform with one sample for each zero-based tick.
	FiniteRumbleEffect byTick(IntFunction<RumbleState> state, int durationTicks);

	/// Replaces the waveform, sampling time in [0, 1), as tick / duration.
	FiniteRumbleEffect byTime(Function<Float, RumbleState> state, int durationTicks);

	FiniteRumbleEffect constant(float strong, float weak, int durationTicks);

	/// Appends another waveform, retaining this effect's stop conditions.
	FiniteRumbleEffect then(FiniteRumbleEffect next);

	FiniteRumbleEffect repeat(int count);
}
