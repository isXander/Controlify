/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.UnaryEventStage;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.DoubleSupplier;

/// Expands the domain of physical inputs.
/// - Expands button presses and releases into axis moved events.
/// - Expands axis moved events into button presses and releases when passing threshold.
public class InputDomainExpansionStage implements UnaryEventStage<PhysicalSignal> {

	private final Set<Identifier> pressedButtons;
	private DoubleSupplier actuation, release;
	private final float pressedValue, releasedValue;

	public InputDomainExpansionStage(float actuation, float release, float pressedValue, float releasedValue) {
		this.pressedButtons = new HashSet<>();
		this.actuation = () -> actuation;
		this.release = () -> release;
		this.pressedValue = pressedValue;
		this.releasedValue = releasedValue;
	}

	public void setThresholdSupplier(DoubleSupplier threshold) {
		this.actuation = threshold;
		this.release = () -> Math.max(0, threshold.getAsDouble() - 0.01);
	}

	public void resetInputState() {
		this.pressedButtons.clear();
	}

	@Override
	public void onEvent(@NonNull PhysicalSignal event, @NonNull EventSink<? super PhysicalSignal> downstream) {
		downstream.accept(event);

		switch (event) {
			case PhysicalInputSignal.AxisMoved(long timeNanos, Identifier input, float value) -> {
				if (value >= this.actuation.getAsDouble()) {
					if (this.pressedButtons.add(input)) {
						downstream.accept(new PhysicalInputSignal.ButtonChanged(timeNanos, input, true));
					}
				} else if (value <= this.release.getAsDouble()) {
					if (this.pressedButtons.remove(input)) {
						downstream.accept(new PhysicalInputSignal.ButtonChanged(timeNanos, input, false));
					}
				}
			}
			case PhysicalInputSignal.ButtonChanged(long timeNanos, Identifier input, boolean state) -> {
				downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, input, state ? pressedValue : releasedValue));
			}
			default -> {}
		}
	}
}
