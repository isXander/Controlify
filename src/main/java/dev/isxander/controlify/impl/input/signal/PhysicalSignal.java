/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.signal;

public sealed interface PhysicalSignal permits PhysicalInputSignal, PhysicalSensorSignal, PhysicalSignal.Frame {
	/// timestamp of the signal in nanoseconds
	long timeNanos();

	/// Emitted at the end of each polling / every frame.
	record Frame(long timeNanos, long deltaTimeNanos) implements PhysicalSignal {}
}
