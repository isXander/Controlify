/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.impl.input.mapping.DeadzoneGroup;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeadzoneStageTest {
	private static final Identifier UP = id("axis/right_stick_up");
	private static final Identifier DOWN = id("axis/right_stick_down");
	private static final Identifier LEFT = id("axis/right_stick_left");
	private static final Identifier RIGHT = id("axis/right_stick_right");

	@Test
	void movementInsideRadialDeadzoneNeverActuatesButtons() {
		var stage = stage(0.12f).andThen(new InputDomainExpansionStage(0.5f, 0.49f, 1f, 0f));
		List<PhysicalSignal> output = new ArrayList<>();

		// Jitter in both dimensions, including releasing the previous half-axis on sign changes.
		for (int i = 0; i < 100; i++) {
			stage.onEvent(new PhysicalInputSignal.AxisMoved(i, UP, 0.10f), output::add);
			stage.onEvent(new PhysicalInputSignal.AxisMoved(i, LEFT, 0.01f), output::add);
			stage.onEvent(new PhysicalInputSignal.AxisMoved(i, UP, 0f), output::add);
			stage.onEvent(new PhysicalInputSignal.AxisMoved(i, DOWN, 0.08f), output::add);
			stage.onEvent(new PhysicalInputSignal.AxisMoved(i, LEFT, 0f), output::add);
			stage.onEvent(new PhysicalInputSignal.AxisMoved(i, RIGHT, 0.02f), output::add);
			stage.onEvent(new PhysicalInputSignal.AxisMoved(i, DOWN, 0f), output::add);
			stage.onEvent(new PhysicalInputSignal.AxisMoved(i, RIGHT, 0f), output::add);
		}

		assertTrue(output.isEmpty(), () -> "Unexpected output within deadzone: " + output);
	}

	@Test
	void returningInsideDeadzoneReleasesEveryActiveAxis() {
		var stage = stage(0.12f);
		List<PhysicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.AxisMoved(1, UP, 0.8f), output::add);
		stage.onEvent(new PhysicalInputSignal.AxisMoved(2, LEFT, 0.3f), output::add);
		stage.onEvent(new PhysicalInputSignal.AxisMoved(3, LEFT, 0.01f), output::add);
		output.clear();

		stage.onEvent(new PhysicalInputSignal.AxisMoved(4, UP, 0.1f), output::add);

		assertEquals(List.of(
				new PhysicalInputSignal.AxisMoved(4, UP, 0f),
				new PhysicalInputSignal.AxisMoved(4, LEFT, 0f)
		), output);
	}

	@Test
	void rescalesRadialMagnitudeAndPreservesDirection() {
		var stage = stage(0.2f);
		List<PhysicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.AxisMoved(1, UP, 0.3f), output::add);
		output.clear();
		stage.onEvent(new PhysicalInputSignal.AxisMoved(2, RIGHT, 0.4f), output::add);

		assertEquals(2, output.size());
		assertEquals(0.225f, ((PhysicalInputSignal.AxisMoved) output.get(0)).value(), 0.000001f);
		assertEquals(0.3f, ((PhysicalInputSignal.AxisMoved) output.get(1)).value(), 0.000001f);
	}

	private static DeadzoneStage stage(float threshold) {
		var stage = new DeadzoneStage(Set.of(new DeadzoneGroup(id("right_stick"), List.of(UP, DOWN, LEFT, RIGHT))));
		stage.setDeadzoneSupplier(_ -> threshold);
		return stage;
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("controlify", path);
	}
}
