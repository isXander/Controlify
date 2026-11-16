/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import dev.isxander.controlify.api.ingame.look.LookContext;
import dev.isxander.controlify.api.ingame.look.LookOutput;
import dev.isxander.controlify.api.ingame.look.LookSource;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.config.settings.profile.FlickStickSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import org.jspecify.annotations.NonNull;

/// Flick stick: pushing the stick turns the camera to face that direction, released over a short
/// time with an ease-out. While the stick stays deflected afterwards, turning the stick turns the
/// camera 1:1. Both leave as [LookOutput#scripted], so scalers cannot change them.
public final class FlickStick implements LookSource {
	private final ControllerImpl controller;

	private boolean flicking;
	private double flickYaw;
	private double elapsedSeconds;
	private double releasedYaw;

	/// The stick is deflected after a flick, so its angle is being followed.
	private boolean engaged;
	private float lastStickAngle;

	public FlickStick(ControllerImpl controller) {
		this.controller = controller;
	}

	/// Flick stick replaces the regular stick look when it is enabled.
	public static boolean isEnabled(ControllerImpl controller) {
		return controller.settings().flickStick.enabled;
	}

	@Override
	public void contribute(@NonNull LookContext ctx, @NonNull LookOutput out) {
		FlickStickSettings settings = controller.settings().flickStick;
		if (!settings.enabled) {
			this.reset();
			return;
		}

		float y = ControlifyActions.LOOK_DOWN.on(controller).continuous().getContinuous()
				- ControlifyActions.LOOK_UP.on(controller).continuous().getContinuous();
		float x = ControlifyActions.LOOK_RIGHT.on(controller).continuous().getContinuous()
				- ControlifyActions.LOOK_LEFT.on(controller).continuous().getContinuous();
		boolean deflected = x != 0 || y != 0;
		float stickAngle = stickAngle(x, y);

		// every pulse must be consumed, so no short-circuiting
		boolean triggered = ControlifyActions.LOOK_DOWN.on(controller).pulse().consumeAllPulses()
				| ControlifyActions.LOOK_UP.on(controller).pulse().consumeAllPulses()
				| ControlifyActions.LOOK_LEFT.on(controller).pulse().consumeAllPulses()
				| ControlifyActions.LOOK_RIGHT.on(controller).pulse().consumeAllPulses();

		double yaw = 0;

		Step step = decide(triggered, this.engaged, deflected, settings.rotate);

		if (step == Step.FLICK) {
			// a new flick finishes the one before it immediately
			if (this.flicking) {
				yaw += this.flickYaw - this.releasedYaw;
				this.flicking = false;
			}

			if (!isInForwardDeadzone(stickAngle, settings.forwardDeadzone)) {
				this.flicking = true;
				this.flickYaw = snap(stickAngle, settings.snap);
				this.elapsedSeconds = 0;
				this.releasedYaw = 0;
			}

			this.engaged = true;
			this.lastStickAngle = stickAngle;
		} else if (step == Step.RELEASE) {
			this.engaged = false;
		} else if (step == Step.ROTATE) {
			yaw += angleDelta(this.lastStickAngle, stickAngle);
			this.lastStickAngle = stickAngle;
		}

		if (this.flicking) {
			this.elapsedSeconds += ctx.dt();

			double releasedNow = this.flickYaw * releaseProgress(this.elapsedSeconds, settings.flickTime);
			yaw += releasedNow - this.releasedYaw;
			this.releasedYaw = releasedNow;

			if (this.elapsedSeconds >= settings.flickTime) {
				this.flicking = false;
			}
		}

		if (yaw != 0) {
			out.scripted(yaw, 0);
		}
	}

	@Override
	public void reset() {
		this.flicking = false;
		this.flickYaw = 0;
		this.elapsedSeconds = 0;
		this.releasedYaw = 0;
		this.engaged = false;
	}

	/// What a frame of stick input does to the flick stick.
	enum Step {
		/// start a new flick
		FLICK,
		/// follow the stick's turn
		ROTATE,
		/// the stick returned to neutral
		RELEASE,
		/// nothing
		IDLE
	}

	/// A flick only starts when the stick was not already engaged, so a different direction
	/// crossing its threshold while rotating does not start another one.
	static Step decide(boolean triggered, boolean engaged, boolean deflected, boolean rotate) {
		if (triggered && !engaged) {
			return Step.FLICK;
		}
		if (!deflected) {
			return Step.RELEASE;
		}
		if (engaged && rotate) {
			return Step.ROTATE;
		}
		return Step.IDLE;
	}

	/// @return the direction the stick points in degrees, 0 being straight forward (stick up)
	/// and positive turning right, in the range [-180, 180)
	static float stickAngle(float x, float y) {
		return wrapDegrees((float) Math.toDegrees(Math.atan2(y, x)) + 90f);
	}

	/// @return whether a flick this far from straight forward should do nothing
	static boolean isInForwardDeadzone(float angleDegrees, float deadzoneDegrees) {
		return Math.abs(angleDegrees) < deadzoneDegrees;
	}

	/// @return the angle rounded to the nearest step of the snap, or unchanged for [FlickSnap#NONE]
	static float snap(float angleDegrees, FlickSnap snap) {
		float step = snap.stepDegrees();
		if (step <= 0) {
			return angleDegrees;
		}
		return wrapDegrees(Math.round(angleDegrees / step) * step);
	}

	/// @return the signed shortest turn from one angle to another, in the range [-180, 180)
	static float angleDelta(float fromDegrees, float toDegrees) {
		return wrapDegrees(toDegrees - fromDegrees);
	}

	/// @return how much of a flick has been released, 0 to 1 on an ease-out-expo curve.
	/// Once the time is up it is exactly 1, so the flick lands on its target.
	static double releaseProgress(double elapsedSeconds, double flickTimeSeconds) {
		if (flickTimeSeconds <= 0 || elapsedSeconds >= flickTimeSeconds) {
			return 1;
		}
		if (elapsedSeconds <= 0) {
			return 0;
		}
		return 1 - Math.pow(2, -10 * (elapsedSeconds / flickTimeSeconds));
	}

	static float wrapDegrees(float degrees) {
		float wrapped = degrees % 360f;
		if (wrapped >= 180f) {
			wrapped -= 360f;
		}
		if (wrapped < -180f) {
			wrapped += 360f;
		}
		return wrapped;
	}
}
