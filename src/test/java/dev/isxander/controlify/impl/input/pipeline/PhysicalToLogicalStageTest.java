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
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
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

	@Test
	void loneTapWaitsForTheDoubleTapWindow() {
		var stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());
		List<LogicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(0, A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(100), A, false), output::add);
		stage.onEvent(new PhysicalSignal.Frame(ms(300), ms(200)), output::add);
		assertEquals(0, count(output, LogicalSignal.Tapped.class));

		// The gap is measured from the release, so the tap resolves 200ms after it.
		stage.onEvent(new PhysicalSignal.Frame(ms(301), ms(1)), output::add);
		assertEquals(1, count(output, LogicalSignal.Tapped.class));
		assertEquals(0, count(output, LogicalSignal.DoubleTapped.class));
	}

	@Test
	void doubleTapProducesNoTaps() {
		var stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());
		List<LogicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(0, A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(100), A, false), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(200), A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(250), A, false), output::add);
		stage.onEvent(new PhysicalSignal.Frame(ms(1000), ms(750)), output::add);

		assertEquals(1, count(output, LogicalSignal.DoubleTapped.class));
		assertEquals(0, count(output, LogicalSignal.Tapped.class));
	}

	@Test
	void tapFollowedByLongPressFlushesTheTapBeforeTheRelease() {
		var stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());
		List<LogicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(0, A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(100), A, false), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(150), A, true), output::add);
		output.clear();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(390), A, false), output::add);

		assertEquals(List.of(LogicalSignal.Tapped.class, LogicalSignal.ButtonRelease.class, LogicalSignal.GuiPress.class),
				output.stream().map(Object::getClass).toList());
	}

	@Test
	void slowSecondPressFlushesTheTapBeforeItsPress() {
		var stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());
		List<LogicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(0, A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(100), A, false), output::add);
		output.clear();

		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(350), A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(400), A, false), output::add);

		assertEquals(List.of(LogicalSignal.Tapped.class, LogicalSignal.ButtonPress.class,
						LogicalSignal.ButtonRelease.class, LogicalSignal.GuiPress.class),
				output.stream().map(Object::getClass).toList());
	}

	@Test
	void secondPressHeldResolvesTheTapBeforeTheHold() {
		var stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());
		List<LogicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(0, A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(100), A, false), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(250), A, true), output::add);

		// Past the gap, but the second press could still be a tap.
		stage.onEvent(new PhysicalSignal.Frame(ms(400), ms(150)), output::add);
		assertEquals(0, count(output, LogicalSignal.Tapped.class));

		output.clear();
		stage.onEvent(new PhysicalSignal.Frame(ms(451), ms(51)), output::add);
		assertEquals(List.of(LogicalSignal.Tapped.class, LogicalSignal.Frame.class),
				output.stream().map(Object::getClass).toList());

		output.clear();
		stage.onEvent(new PhysicalSignal.Frame(ms(650), ms(199)), output::add);
		assertEquals(List.of(LogicalSignal.Held.class, LogicalSignal.Frame.class),
				output.stream().map(Object::getClass).toList());
	}

	@Test
	void timingsApplyFromTheSupplier() {
		var stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());
		stage.setTimingsSupplier(() -> GestureTimings.ofMillis(100, 50, 300));
		List<LogicalSignal> output = new ArrayList<>();
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(0, A, true), output::add);
		stage.onEvent(new PhysicalInputSignal.ButtonChanged(ms(150), A, false), output::add);
		stage.onEvent(new PhysicalSignal.Frame(ms(1000), ms(850)), output::add);

		// 150ms is longer than the configured 100ms tap.
		assertEquals(0, count(output, LogicalSignal.Tapped.class));
	}

	private static long ms(long millis) {
		return millis * 1_000_000L;
	}

	private static long count(List<LogicalSignal> output, Class<? extends LogicalSignal> type) {
		return output.stream().filter(type::isInstance).count();
	}

	private static LogicalInput chord(Identifier trigger, Identifier... modifiers) {
		return new LogicalInput.ModifiedChord(Set.of(modifiers), trigger);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}
}
