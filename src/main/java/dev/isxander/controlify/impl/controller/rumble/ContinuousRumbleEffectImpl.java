/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.rumble;

import dev.isxander.controlify.api.controller.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class ContinuousRumbleEffectImpl implements ContinuousRumbleEffect {
	private final IntFunction<RumbleState> state;
	private final BooleanSupplier stop;
	private final int timeout, minimum;
	private final UnaryOperator<RumbleState> attenuation;

	public ContinuousRumbleEffectImpl() {
		this(_ -> RumbleState.NONE, () -> false, 0, 0, value -> value);
	}

	private ContinuousRumbleEffectImpl(IntFunction<RumbleState> state, BooleanSupplier stop,
			int timeout, int minimum, UnaryOperator<RumbleState> attenuation) {
		this.state = Objects.requireNonNull(state);
		this.stop = stop;
		this.timeout = timeout;
		this.minimum = minimum;
		this.attenuation = attenuation;
	}

	@Override
	public int timeoutTicks() {
		return this.timeout;
	}

	@Override
	public int minimumTicks() {
		return this.minimum;
	}

	@Override
	public boolean shouldStop() {
		return this.stop.getAsBoolean();
	}

	@Override
	public @NonNull RumbleState stateAt(int tick) {
		return this.attenuation.apply(Objects.requireNonNull(this.state.apply(tick)));
	}

	@Override
	public @NonNull ContinuousRumbleEffect byTick(@NonNull IntFunction<RumbleState> state) {
		return new ContinuousRumbleEffectImpl(state, this.stop, this.timeout, this.minimum, this.attenuation);
	}

	@Override
	public @NonNull ContinuousRumbleEffect constant(float strong, float weak) {
		return byTick(_ -> new RumbleState(strong, weak));
	}

	@Override
	public @NonNull ContinuousRumbleEffect stopCondition(@NonNull BooleanSupplier condition) {
		Objects.requireNonNull(condition);
		return new ContinuousRumbleEffectImpl(
				this.state,
				() -> this.stop.getAsBoolean() || condition.getAsBoolean(),
				this.timeout,
				this.minimum,
				this.attenuation
		);
	}

	@Override
	public @NonNull ContinuousRumbleEffect timeout(int ticks) {
		if (ticks < 0) {
			throw new IllegalArgumentException("Timeout cannot be negative");
		}
		return new ContinuousRumbleEffectImpl(this.state, this.stop, ticks, this.minimum, this.attenuation);
	}

	@Override
	public @NonNull ContinuousRumbleEffect minTime(int ticks) {
		if (ticks < 0) {
			throw new IllegalArgumentException("Minimum duration cannot be negative");
		}
		return new ContinuousRumbleEffectImpl(this.state, this.stop, this.timeout, ticks, this.attenuation);
	}

	@Override
	public @NonNull ContinuousRumbleEffect inWorld(
			@NonNull Supplier<Vec3> location,
			float minMagnitude, float maxMagnitude,
			float range,
			@NonNull Function<Float, Float> falloff
	) {
		Objects.requireNonNull(location);
		Objects.requireNonNull(falloff);
		if (!Float.isFinite(range) || range <= 0) {
			throw new IllegalArgumentException("Range must be finite and positive");
		}
		if (!Float.isFinite(minMagnitude) || !Float.isFinite(maxMagnitude) || minMagnitude < 0 || maxMagnitude < minMagnitude) {
			throw new IllegalArgumentException("Magnitudes must be finite, nonnegative and ordered");
		}
		return new ContinuousRumbleEffectImpl(
				this.state,
				() -> this.stop.getAsBoolean() || Minecraft.getInstance().getCameraEntity() == null,
				this.timeout, this.minimum, value -> {
					var camera = Minecraft.getInstance().getCameraEntity();
					if (camera == null) {
						return RumbleState.NONE;
					}
					float distance = (float) Mth.clamp(camera.distanceToSqr(location.get()) / ((double) range * range), 0, 1);
					return value.mul(Mth.lerp(falloff.apply(1 - distance), minMagnitude, maxMagnitude));
				}
		);
	}
}
