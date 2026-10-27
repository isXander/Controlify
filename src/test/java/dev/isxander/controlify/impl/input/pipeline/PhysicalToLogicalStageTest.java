/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhysicalToLogicalStageTest {
	private static final Identifier A = id("a");
	private static final Identifier B = id("b");
	private static final Identifier C = id("c");
	private static final Identifier D = id("d");

	@Test
	void pressEmitsChordCandidatesMostSpecificFirst() {
		var stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());
		List<LogicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(1, A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(2, B, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(3, C, true), output::add);
		output.clear();

		stage.onEvent(new PhysicalInputSignal.ButtonChanged(4, D, true), output::add);

		assertEquals(List.of(
				chord(D, C, B),
				chord(D, C, A),
				chord(D, B, A),
				chord(D, C),
				chord(D, B),
				chord(D, A),
				new LogicalInput.Single(D)
		), output.stream()
				.map(signal -> ((LogicalSignal.ButtonPress) signal).input())
				.toList());
	}

	@Test
	void releasingAModifierReleasesMostSpecificChordFirst() {
		var stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());
		List<LogicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(1, A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(2, B, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(3, C, true), output::add);
		output.clear();

		stage.onEvent(new PhysicalInputSignal.ButtonChanged(4, A, false), output::add);

		// Equally specific chords keep press order.
		assertEquals(List.of(chord(C, B, A), chord(B, A), chord(C, A), new LogicalInput.Single(A)), output.stream()
				.filter(signal -> signal instanceof LogicalSignal.ButtonRelease)
				.map(signal -> ((LogicalSignal.ButtonRelease) signal).input())
				.toList());
	}

	private static LogicalInput chord(Identifier trigger, Identifier... modifiers) {
		return new LogicalInput.ModifiedChord(Set.of(modifiers), trigger);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}
}
