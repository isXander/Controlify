package dev.isxander.controlify.impl.controller.rumble;

import dev.isxander.controlify.api.controller.rumble.FiniteRumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class FiniteRumbleEffectImpl implements FiniteRumbleEffect {
	private final RumbleState[] frames;
	private final BooleanSupplier stop;

	public FiniteRumbleEffectImpl() {
		this(new RumbleState[0], () -> false);
	}

	private FiniteRumbleEffectImpl(RumbleState[] frames, BooleanSupplier stop) {
		this.frames = frames;
		this.stop = stop;
	}

	@Override
	public int durationTicks() {
		return this.frames.length;
	}

	@Override
	public @NonNull RumbleState stateAt(int tick) {
		return this.frames[tick];
	}

	@Override
	public boolean shouldStop() {
		return this.stop.getAsBoolean();
	}

	@Override
	public @NonNull FiniteRumbleEffect stopCondition(@NonNull BooleanSupplier condition) {
		Objects.requireNonNull(condition);
		return new FiniteRumbleEffectImpl(this.frames, () -> this.stop.getAsBoolean() || condition.getAsBoolean());
	}

	@Override
	public @NonNull FiniteRumbleEffect byTick(@NonNull IntFunction<RumbleState> state, int durationTicks) {
		Objects.requireNonNull(state);
		if (durationTicks < 0) {
			throw new IllegalArgumentException("Duration cannot be negative");
		}
		RumbleState[] frames = new RumbleState[durationTicks];
		for (int i = 0; i < frames.length; i++) {
			frames[i] = Objects.requireNonNull(state.apply(i));
		}
		return new FiniteRumbleEffectImpl(frames, this.stop);
	}

	@Override
	public @NonNull FiniteRumbleEffect byTime(@NonNull Function<Float, RumbleState> state, int durationTicks) {
		Objects.requireNonNull(state);
		return byTick(tick -> state.apply((float) tick / durationTicks), durationTicks);
	}

	@Override
	public @NonNull FiniteRumbleEffect constant(float strong, float weak, int durationTicks) {
		return byTick(_ -> new RumbleState(strong, weak), durationTicks);
	}

	@Override
	public @NonNull FiniteRumbleEffect then(@NonNull FiniteRumbleEffect next) {
		Objects.requireNonNull(next);
		int length = next.durationTicks();
		if (length < 0) {
			throw new IllegalArgumentException("Duration cannot be negative");
		}

		RumbleState[] frames = Arrays.copyOf(this.frames, Math.addExact(this.frames.length, length));
		for (int i = 0; i < length; i++) {
			frames[this.frames.length + i] = Objects.requireNonNull(next.stateAt(i));
		}
		return new FiniteRumbleEffectImpl(frames, this.stop);
	}

	@Override
	public @NonNull FiniteRumbleEffect repeat(int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("Repeat count must be positive");
		}
		RumbleState[] frames = new RumbleState[Math.multiplyExact(this.frames.length, count)];
		for (int i = 0; i < count && this.frames.length > 0; i++) {
			System.arraycopy(this.frames, 0, frames, i * this.frames.length, this.frames.length);
		}
		return new FiniteRumbleEffectImpl(frames, this.stop);
	}
}
