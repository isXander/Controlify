/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

/// Accepts action state.
public interface Accumulator {
	default void firePulse() {
	}

	default void toggleLatch() {
	}
	default void setLatch(boolean active) {
	}

	default void setContinuous(float value) {
	}
}
