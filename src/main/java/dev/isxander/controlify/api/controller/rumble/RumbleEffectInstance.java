/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.controller.rumble;

/// A handle on a specific instance of a queued, playing, or played rumble
/// effect.
public interface RumbleEffectInstance {
	/// @return true if the queued rumble effect is playing, or has finished playing
	boolean hasStarted();

	/// @return true if queued rumble effect has finished playing.
	boolean hasFinished();

	/// Stops the effect before it has finished.
	///
	/// @return true if the effect was stopped as a result of this method invocation.
	boolean stop();

	/// Resets the age of this effect instance.
	/// Only applicable for continuous effects, it prevents
	/// them from timing out. This should be called frequently in the
	/// context that the effect should stay viable.
	/// If the effect has already finished playing, the effect cannot be kept
	/// alive as it is already dead.
	///
	/// @return true if the effect was successfully kept alive
	boolean keepAlive();
}
