/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils;

/// Consumer-local repetition for shared actions whose general-purpose activator must not repeat.
/// Call once per fixed update, including while inactive. Delays are in fixed-update ticks.
/// Dedicated repeat actions should use the repeat-latch activator instead.
public final class HoldRepeatHelper {
	private final int initialDelay;
	private final int repeatDelay;
	private int remainingDelay;
	private boolean previouslyActive;

	public HoldRepeatHelper(int initialDelay, int repeatDelay) {
		this.initialDelay = initialDelay;
		this.repeatDelay = repeatDelay;
	}

	public boolean shouldAction(boolean active) {
		if (!active) {
			this.reset();
			return false;
		}
		if (!this.previouslyActive) {
			this.previouslyActive = true;
			this.remainingDelay = this.initialDelay;
			return true;
		}
		if (--this.remainingDelay <= 0) {
			this.remainingDelay = this.repeatDelay;
			return true;
		}
		return false;
	}

	public void reset() {
		this.previouslyActive = false;
		this.remainingDelay = 0;
	}
}
