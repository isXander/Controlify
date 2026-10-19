package dev.isxander.controlify.api.controller.rumble;

import net.minecraft.world.phys.Vec3;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/// An immutable effect definition with independent timing for every playback.
public non-sealed interface ContinuousRumbleEffect extends RumbleEffect {
	@Override ContinuousRumbleEffect stopCondition(BooleanSupplier condition);

	ContinuousRumbleEffect byTick(IntFunction<RumbleState> state);

	ContinuousRumbleEffect constant(float strong, float weak);

	/// Sets the inactivity timeout. Zero disables it; keepAlive resets its clock.
	ContinuousRumbleEffect timeout(int ticks);
	int timeoutTicks();

	/// Sets the minimum duration before timeout or a stop condition can finish playback.
	/// Explicitly stopping the playback handle always takes effect immediately.
	ContinuousRumbleEffect minTime(int ticks);
	int minimumTicks();

	/// Applies distance attenuation relative to the camera, using squared distance / range².
	/// Playback ends when no camera is available (subject to the minimum duration).
	ContinuousRumbleEffect inWorld(
			Supplier<Vec3> location,
			float minMagnitude, float maxMagnitude,
			float range,
			Function<Float, Float> falloff
	);
}
