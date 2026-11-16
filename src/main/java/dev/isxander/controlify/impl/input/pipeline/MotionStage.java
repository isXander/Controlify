/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.EventStage;
import dev.isxander.controlify.impl.input.SensorType;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSensorSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

/// Turns gyroscope (and optionally accelerometer) samples into look deltas, per sensor sample.
///
/// FPS-style gyro processing (gravity fusion, space transforms, smoothing, tightening, acceleration)
/// is nonlinear, so it runs on every sample before integration; otherwise results would depend on frame rate.
/// This stage has no knowledge of bindings or actions.
///
/// Integration is hold-last-sample: a sample is held until the next sample arrives, and the segment between
/// the two is processed as one indivisible unit. The segment still being held when a frame ends is processed
/// when its successor arrives (at most one sensor interval later), so the total output never depends on where
/// frame boundaries fall. State resets after a gap of more than 50ms, like {@link SensorAccumulatorStage}.
///
/// Conventions:
/// - Gyro samples are in rad/s, accelerometer samples in m/s^2, as delivered by SDL.
/// - The accelerometer measures reaction to gravity, so at rest it reads a vector pointing up (towards the sky)
///   in the controller frame. SDL's controller frame is X right, Y up, Z towards the player when the controller is
///   held upright in front of you, so the neutral at-rest reading is (0, +9.81, 0).
///   The gravity estimate is stored and emitted in this convention (as a unit vector pointing up).
/// - Output yaw/pitch use the look impulse convention: positive yaw looks right, positive pitch looks down.
///   {@link dev.isxander.controlify.impl.controller.gyro.GyroSpace#LOCAL_YAW} is therefore {@code -omega.y},
///   {@code LOCAL_ROLL} is {@code -omega.z} and pitch is {@code -omega.x}.
public class MotionStage implements EventStage<PhysicalSignal, LogicalSignal.MotionInterval> {
	private static final long MAX_SAMPLE_AGE_NANOS = 50_000_000L;
	private static final double RAD_TO_DEG = 180.0 / Math.PI;
	private static final double STANDARD_GRAVITY = 9.81;

	/// Gravity correction towards the accelerometer, per second, when the accelerometer is perfectly steady.
	private static final double GRAVITY_CORRECTION_RATE = 2.0;
	/// Time constant of the accelerometer steadiness statistics.
	private static final double ACCEL_STATS_TIME_CONSTANT = 0.1;
	/// Smoothed accelerometer deviation (m/s^2) at which gravity correction stops entirely.
	private static final double SHAKE_LIMIT = 1.5;
	/// GyroWiki player space factor.
	private static final double PLAYER_SPACE_YAW_RELAX = 1.41;

	private Supplier<MotionParams> paramsSupplier = () -> MotionParams.DEFAULT;
	/// Read once per event so a settings change never splits one event's decisions.
	private MotionParams params = MotionParams.DEFAULT;

	// Gyro integration
	private long lastGyroNanos = -1;
	private boolean hasHeld;
	private double heldX, heldY, heldZ;
	private long originNanos;

	private double pendingYawDeg;
	private double pendingPitchDeg;
	private long pendingNanos;

	// Gravity
	private boolean hasGravity;
	private double gravX, gravY, gravZ;

	// Accelerometer
	private boolean hasAccel;
	private long latestAccelNanos;
	private double accelX, accelY, accelZ;
	private double smoothedAccelX, smoothedAccelY, smoothedAccelZ;
	private double smoothedDeviation;
	private double smoothedMagnitudeDeviation;
	private double steadiness;

	// Smoothing
	private final ArrayDeque<Knot> knots = new ArrayDeque<>();
	private final double[] evalScratch = new double[4];

	/// Parameters are read on every event, so changes apply immediately.
	public void setParamsSupplier(Supplier<MotionParams> paramsSupplier) {
		this.paramsSupplier = Objects.requireNonNull(paramsSupplier);
		this.params = Objects.requireNonNull(paramsSupplier.get());
	}

	/// Drops all motion history, including the gravity estimate.
	public void reset() {
		this.resetGyro();
		this.hasGravity = false;
		this.hasAccel = false;
	}

	@Override
	public void onEvent(
			@NonNull PhysicalSignal event,
			@NonNull EventSink<? super LogicalSignal.MotionInterval> downstream
	) {
		this.params = Objects.requireNonNull(this.paramsSupplier.get());
		switch (event) {
			case PhysicalSignal.Frame(long timeNanos, _) -> {
				if (this.pendingNanos > 0) {
					downstream.accept(new LogicalSignal.MotionInterval(
							timeNanos,
							this.pendingNanos,
							(float) this.pendingYawDeg,
							(float) this.pendingPitchDeg,
							(float) this.gravX,
							(float) this.gravY,
							(float) this.gravZ,
							this.hasGravity
					));
				}
				this.pendingYawDeg = 0;
				this.pendingPitchDeg = 0;
				this.pendingNanos = 0;
			}
			case PhysicalSensorSignal(long timeNanos, _, SensorType type, float x, float y, float z)
					when type == SensorType.GYROSCOPE -> this.onGyro(timeNanos, x, y, z);
			case PhysicalSensorSignal(long timeNanos, _, SensorType type, float x, float y, float z)
					when type == SensorType.ACCELEROMETER -> this.onAccel(timeNanos, x, y, z);
			default -> {
			}
		}
	}

	private void onAccel(long timeNanos, double x, double y, double z) {
		double length = Math.sqrt(x * x + y * y + z * z);
		if (!Double.isFinite(length) || length < 1e-3) {
			return;
		}

		boolean continuing = this.hasAccel && timeNanos - this.latestAccelNanos <= MAX_SAMPLE_AGE_NANOS;
		double dtSeconds = continuing ? (timeNanos - this.latestAccelNanos) * 1e-9 : 0;
		if (!continuing) {
			this.smoothedAccelX = x;
			this.smoothedAccelY = y;
			this.smoothedAccelZ = z;
			this.smoothedDeviation = 0;
			this.smoothedMagnitudeDeviation = Math.abs(length - STANDARD_GRAVITY);
		} else if (dtSeconds > 0) {
			double alpha = 1 - Math.exp(-dtSeconds / ACCEL_STATS_TIME_CONSTANT);
			double dx = x - this.smoothedAccelX;
			double dy = y - this.smoothedAccelY;
			double dz = z - this.smoothedAccelZ;
			double deviation = Math.sqrt(dx * dx + dy * dy + dz * dz);
			this.smoothedAccelX += dx * alpha;
			this.smoothedAccelY += dy * alpha;
			this.smoothedAccelZ += dz * alpha;
			this.smoothedDeviation += (deviation - this.smoothedDeviation) * alpha;
			this.smoothedMagnitudeDeviation += (Math.abs(length - STANDARD_GRAVITY) - this.smoothedMagnitudeDeviation) * alpha;
		}

		this.accelX = x;
		this.accelY = y;
		this.accelZ = z;
		this.latestAccelNanos = Math.max(this.latestAccelNanos, timeNanos);
		this.hasAccel = true;
		double shake = Math.max(this.smoothedDeviation, this.smoothedMagnitudeDeviation);
		this.steadiness = clamp(1 - shake / SHAKE_LIMIT, 0, 1);

		if (!this.hasGravity) {
			this.gravX = x / length;
			this.gravY = y / length;
			this.gravZ = z / length;
			this.hasGravity = true;
		}
	}

	private void onGyro(long timeNanos, double x, double y, double z) {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
			return;
		}

		if (this.hasHeld) {
			long deltaNanos = timeNanos - this.lastGyroNanos;
			if (deltaNanos > MAX_SAMPLE_AGE_NANOS) {
				// A gap invalidates the held sample, smoothing history and any motion waiting for a frame.
				this.resetGyro();
				this.reseedGravityAfterGap(timeNanos);
			} else if (deltaNanos > 0) {
				this.processSegment(this.lastGyroNanos, timeNanos);
			}
		}

		if (!this.hasHeld) {
			this.originNanos = timeNanos;
			this.lastGyroNanos = timeNanos;
		} else {
			this.lastGyroNanos = Math.max(this.lastGyroNanos, timeNanos);
		}
		this.hasHeld = true;
		this.heldX = x;
		this.heldY = y;
		this.heldZ = z;
	}

	private void resetGyro() {
		this.hasHeld = false;
		this.lastGyroNanos = -1;
		this.pendingYawDeg = 0;
		this.pendingPitchDeg = 0;
		this.pendingNanos = 0;
		this.knots.clear();
	}

	/// The controller may have moved during the gap, so the old estimate cannot be rotated forward.
	private void reseedGravityAfterGap(long timeNanos) {
		this.hasGravity = false;
		if (this.hasAccel && timeNanos - this.latestAccelNanos <= MAX_SAMPLE_AGE_NANOS) {
			double length = Math.sqrt(this.accelX * this.accelX + this.accelY * this.accelY + this.accelZ * this.accelZ);
			this.gravX = this.accelX / length;
			this.gravY = this.accelY / length;
			this.gravZ = this.accelZ / length;
			this.hasGravity = true;
		}
	}

	/// Processes the held sample over {@code [startNanos, endNanos]}.
	private void processSegment(long startNanos, long endNanos) {
		MotionParams p = this.params;
		long deltaNanos = endNanos - startNanos;
		double dt = deltaNanos * 1e-9;

		this.updateGravity(dt, endNanos);

		// a. space transform to degrees per second
		double yaw;
		double pitch;
		double wx = this.heldX * RAD_TO_DEG;
		double wy = this.heldY * RAD_TO_DEG;
		double wz = this.heldZ * RAD_TO_DEG;
		pitch = -wx;
		switch (p.space()) {
			case LOCAL_YAW -> yaw = -wy;
			case LOCAL_ROLL -> yaw = -wz;
			case LOCAL_BOTH -> yaw = -wy - wz;
			case WORLD -> {
				if (this.hasGravity) {
					yaw = -(wx * this.gravX + wy * this.gravY + wz * this.gravZ);
					// the controller's X axis made perpendicular to gravity
					double px = 1 - this.gravX * this.gravX;
					double py = -this.gravX * this.gravY;
					double pz = -this.gravX * this.gravZ;
					double length = Math.sqrt(px * px + py * py + pz * pz);
					pitch = length < 1e-6 ? 0 : -(wx * px + wy * py + wz * pz) / length;
				} else {
					yaw = -wy;
				}
			}
			case PLAYER -> {
				if (this.hasGravity) {
					double worldYaw = wy * this.gravY + wz * this.gravZ;
					double magnitude = Math.min(Math.abs(worldYaw) * PLAYER_SPACE_YAW_RELAX, Math.hypot(wy, wz));
					yaw = -Math.copySign(magnitude, worldYaw);
				} else {
					yaw = -wy;
				}
			}
			default -> throw new IllegalStateException();
		}

		// b. soft-tiered smoothing
		double smoothThreshold = p.smoothThreshold();
		double smoothTime = p.smoothTime();
		if (smoothThreshold > 0 && smoothTime > 0) {
			double magnitude = Math.hypot(yaw, pitch);
			double half = smoothThreshold / 2;
			double directWeight = clamp((magnitude - half) / half, 0, 1);
			double directYaw = yaw * directWeight;
			double directPitch = pitch * directWeight;
			double[] smoothed = this.advanceSmoothing(
					(startNanos - this.originNanos) * 1e-9,
					(endNanos - this.originNanos) * 1e-9,
					yaw - directYaw,
					pitch - directPitch,
					smoothTime
			);
			yaw = directYaw + smoothed[0];
			pitch = directPitch + smoothed[1];
		} else {
			this.knots.clear();
		}

		// c. tightening
		double magnitude = Math.hypot(yaw, pitch);
		double tighten = p.tightenThreshold();
		if (tighten > 0 && magnitude < tighten) {
			double scale = magnitude / tighten;
			yaw *= scale;
			pitch *= scale;
			magnitude *= scale;
		}

		// d. sensitivity with acceleration
		double sensitivity = p.sensitivity();
		if (p.accelFastThreshold() > p.accelSlowThreshold() && p.accelFastThreshold() > 0) {
			double t = clamp((magnitude - p.accelSlowThreshold()) / (p.accelFastThreshold() - p.accelSlowThreshold()), 0, 1);
			sensitivity = p.sensitivity() + (p.fastSensitivity() - p.sensitivity()) * t;
		}
		yaw *= sensitivity;
		pitch *= sensitivity * p.verticalRatio();

		// e. integrate
		this.pendingYawDeg += yaw * dt;
		this.pendingPitchDeg += pitch * dt;
		this.pendingNanos += deltaNanos;
	}

	private void updateGravity(double dt, long endNanos) {
		if (!this.hasGravity) {
			return;
		}

		// The controller turned by omega * dt, so the fixed world vector turns by the inverse in the controller frame.
		double wx = this.heldX;
		double wy = this.heldY;
		double wz = this.heldZ;
		double speed = Math.sqrt(wx * wx + wy * wy + wz * wz);
		double angle = speed * dt;
		if (angle > 1e-12) {
			double kx = wx / speed;
			double ky = wy / speed;
			double kz = wz / speed;
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			double dot = kx * this.gravX + ky * this.gravY + kz * this.gravZ;
			double cx = ky * this.gravZ - kz * this.gravY;
			double cy = kz * this.gravX - kx * this.gravZ;
			double cz = kx * this.gravY - ky * this.gravX;
			double nx = this.gravX * cos - cx * sin + kx * dot * (1 - cos);
			double ny = this.gravY * cos - cy * sin + ky * dot * (1 - cos);
			double nz = this.gravZ * cos - cz * sin + kz * dot * (1 - cos);
			this.gravX = nx;
			this.gravY = ny;
			this.gravZ = nz;
		}

		if (this.hasAccel && endNanos - this.latestAccelNanos <= MAX_SAMPLE_AGE_NANOS) {
			double length = Math.sqrt(this.accelX * this.accelX + this.accelY * this.accelY + this.accelZ * this.accelZ);
			// Shaky readings are mostly linear acceleration, which says nothing about which way is down.
			double blend = 1 - Math.exp(-GRAVITY_CORRECTION_RATE * this.steadiness * dt);
			this.gravX += (this.accelX / length - this.gravX) * blend;
			this.gravY += (this.accelY / length - this.gravY) * blend;
			this.gravZ += (this.accelZ / length - this.gravZ) * blend;
		}

		double length = Math.sqrt(this.gravX * this.gravX + this.gravY * this.gravY + this.gravZ * this.gravZ);
		if (length > 1e-9) {
			this.gravX /= length;
			this.gravY /= length;
			this.gravZ /= length;
		} else {
			this.hasGravity = false;
		}
	}

	/// Adds a constant-rate segment to the moving average and returns the mean of the average over that segment.
	///
	/// The moving average at time t is (R(t) - R(t - window)) / window, where R is the running integral of the
	/// smoothed input. Its integral over a segment is evaluated in closed form from the running integral of R,
	/// so the result is exact however segments are later grouped into frames. Time before the first segment is
	/// treated as a continuation of the first rate, so steady input is passed through undiminished.
	private double[] advanceSmoothing(double start, double end, double rateYaw, double ratePitch, double window) {
		Knot previous = this.knots.peekLast();
		Knot knot;
		if (previous == null) {
			knot = new Knot(start, rateYaw, ratePitch, 0, 0, 0, 0);
		} else {
			this.eval(previous, start);
			double[] e = this.evalScratch;
			knot = new Knot(start, rateYaw, ratePitch, e[0], e[1], e[2], e[3]);
		}
		this.knots.addLast(knot);

		double[] atEnd = this.evalCopy(end);
		double[] atWindowStart = this.evalCopy(start - window);
		double[] atWindowEnd = this.evalCopy(end - window);
		double duration = end - start;
		// RR(start) is the new knot's own value
		double yaw = ((atEnd[1] - knot.cumulativeYawSquared) - (atWindowEnd[1] - atWindowStart[1])) / window / duration;
		double pitch = ((atEnd[3] - knot.cumulativePitchSquared) - (atWindowEnd[3] - atWindowStart[3])) / window / duration;

		// Drop knots that can no longer be inside the window of any later segment.
		double cutoff = end - window;
		while (this.knots.size() >= 2) {
			Knot first = this.knots.pollFirst();
			if (this.knots.peekFirst().time <= cutoff) {
				continue;
			}
			this.knots.addFirst(first);
			break;
		}

		return new double[] {yaw, pitch};
	}

	/// @return {R_yaw, RR_yaw, R_pitch, RR_pitch} at time x
	private double[] evalCopy(double x) {
		Knot found = this.knots.peekFirst();
		Iterator<Knot> it = this.knots.descendingIterator();
		while (it.hasNext()) {
			Knot k = it.next();
			if (k.time <= x) {
				found = k;
				break;
			}
		}
		this.eval(found, x);
		return this.evalScratch.clone();
	}

	/// Extrapolates the knot's piecewise-constant rate, in either direction, to time x.
	private void eval(Knot k, double x) {
		double d = x - k.time;
		this.evalScratch[0] = k.cumulativeYaw + k.rateYaw * d;
		this.evalScratch[1] = k.cumulativeYawSquared + k.cumulativeYaw * d + k.rateYaw * d * d / 2;
		this.evalScratch[2] = k.cumulativePitch + k.ratePitch * d;
		this.evalScratch[3] = k.cumulativePitchSquared + k.cumulativePitch * d + k.ratePitch * d * d / 2;
	}

	private static double clamp(double value, double min, double max) {
		return value < min ? min : Math.min(value, max);
	}

	/// The start of a constant-rate segment, with the running integral of the rate (and of that integral) at that time.
	private record Knot(
			double time,
			double rateYaw, double ratePitch,
			double cumulativeYaw, double cumulativeYawSquared,
			double cumulativePitch, double cumulativePitchSquared
	) {
	}
}
