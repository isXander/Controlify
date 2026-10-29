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
import dev.isxander.controlify.impl.input.signal.PhysicalSensorSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.EventStage;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/// Converts physical inputs into single inputs and modifier chords.
/// A press emits every chord it could form with the buttons already held, up to
/// {@link LogicalInput.ModifiedChord#MAX_MODIFIERS} modifiers, followed by the single input.
/// Chords are ordered most specific first: more modifiers, then more recently pressed modifiers.
/// This stage does not know what is bound; consumers choose between the candidates.
/// Each chord keeps its modifiers until any of its buttons is released,
/// and follows the same gesture lifecycle as a single button.
///
/// Button gestures use event timestamps, with holds emitted once on a tick.
/// Tap and double tap are mutually exclusive for an actuation. After a tap is released, the stage
/// waits up to {@link GestureTimings#doubleTapGapNanos()} for the next press. If none comes, the tap is
/// emitted on the next frame. If one does, the tap is emitted as soon as that press can no longer be a
/// tap, and a {@link LogicalSignal.DoubleTapped} replaces both if it is.
/// Call {@link #onGuiNavigate()} when GUI navigation consumes a pending press.
/// Sensor integration is delegated to {@link SensorAccumulatorStage}.
public class PhysicalToLogicalStage implements EventStage<PhysicalSignal, LogicalSignal> {
	private final SensorAccumulatorStage sensorAccumulatorStage;
	private Supplier<GestureTimings> timingsSupplier;
	/// Read once per event so a settings change never splits one event's decisions.
	private GestureTimings timings;

	// Press order lets an older held button become the latest modifier again when a newer one is released.
	private final LinkedHashSet<Identifier> pressedButtons = new LinkedHashSet<>();
	private final Map<LogicalInput, ButtonState> activeButtons = new LinkedHashMap<>();
	/// Logical input -> release time of a tap waiting to see whether it becomes a double tap.
	private final Map<LogicalInput, Long> pendingTaps = new LinkedHashMap<>();
	private final Map<Identifier, Float> axisValues = new HashMap<>();

	public PhysicalToLogicalStage(SensorAccumulatorStage sensorAccumulatorStage) {
		this(sensorAccumulatorStage, GestureTimings.DEFAULT);
	}

	public PhysicalToLogicalStage(SensorAccumulatorStage sensorAccumulatorStage, GestureTimings timings) {
		this.sensorAccumulatorStage = Objects.requireNonNull(sensorAccumulatorStage);
		this.setTimingsSupplier(() -> timings);
	}

	/// Timings are read on every event, so changes apply immediately.
	public void setTimingsSupplier(Supplier<GestureTimings> timingsSupplier) {
		this.timingsSupplier = Objects.requireNonNull(timingsSupplier);
		this.timings = timingsSupplier.get();
	}

	/// Clears pending gestures and axis values after the physical mapping changes.
	public void resetInputState() {
		this.pressedButtons.clear();
		this.activeButtons.clear();
		this.pendingTaps.clear();
		this.axisValues.clear();
	}

	/// Cancels GUI presses for buttons already held; subsequent presses remain eligible.
	public void onGuiNavigate() {
		this.activeButtons.values().forEach(state -> state.guiPressEligible = false);
	}

	@Override
	public void onEvent(@NonNull PhysicalSignal event, @NonNull EventSink<? super LogicalSignal> downstream) {
		this.timings = this.timingsSupplier.get();
		switch (event) {
			case PhysicalSignal.Frame(long timeNanos, long deltaTimeNanos) -> {
				this.sensorAccumulatorStage.onEvent(event, downstream);
				// Resolve taps before holds: a second press is never held before it stops being a possible tap.
				var pending = this.pendingTaps.entrySet().iterator();
				while (pending.hasNext()) {
					var entry = pending.next();
					ButtonState secondPress = this.activeButtons.get(entry.getKey());
					boolean resolved = secondPress != null && secondPress.secondTap
							? timeNanos - secondPress.pressedAt > this.timings.tapMaxNanos()
							: timeNanos - entry.getValue() > this.timings.doubleTapGapNanos();
					if (resolved) {
						pending.remove();
						downstream.accept(new LogicalSignal.Tapped(timeNanos, entry.getKey()));
					}
				}
				// Holds must fire even without new input events, before downstream actions process this frame's tick.
				this.activeButtons.forEach((input, state) -> {
					if (!state.held && timeNanos - state.pressedAt >= this.timings.holdNanos()) {
						state.held = true;
						downstream.accept(new LogicalSignal.Held(timeNanos, input));
					}
				});
				downstream.accept(new LogicalSignal.Frame(timeNanos, deltaTimeNanos));
			}
			case PhysicalSensorSignal sensorSignal ->
					this.sensorAccumulatorStage.onEvent(sensorSignal, downstream);
			case PhysicalInputSignal.ButtonChanged(long timeNanos, Identifier input, boolean state) ->
					this.onButton(timeNanos, input, state, downstream);
			case PhysicalInputSignal.AxisMoved(long timeNanos, Identifier input, float value) ->
					this.onAxis(timeNanos, input, value, downstream);
		}
	}

	private void onButton(long time, Identifier input, boolean pressed, EventSink<? super LogicalSignal> downstream) {
		if (this.pressedButtons.contains(input) == pressed) {
			// Duplicate edges must not restart gesture timers or change modifier priority.
			return;
		}
		var single = new LogicalInput.Single(input);
		if (pressed) {
			// Only buttons held before this press can modify it; adding the trigger first would select itself.
			for (LogicalInput.ModifiedChord chord : this.recogniseChords(input)) {
				this.press(time, chord, downstream);
			}
			this.press(time, single, downstream);
			this.pressedButtons.add(input);
		} else {
			this.pressedButtons.remove(input);
			// Use the chords captured on press, so later presses cannot change which chord gets released.
			// Snapshot because releasing a chord removes it from activeButtons.
			// Release most specific first, matching press order, so consumers capturing the first release get the largest chord.
			List<LogicalInput.ModifiedChord> released = this.activeButtons.keySet().stream()
					.filter(LogicalInput.ModifiedChord.class::isInstance)
					.map(LogicalInput.ModifiedChord.class::cast)
					.filter(chord -> chord.trigger().equals(input) || chord.modifiers().contains(input))
					.sorted(Comparator.comparingInt((LogicalInput.ModifiedChord chord) -> chord.modifiers().size()).reversed())
					.toList();
			for (var chord : released) {
				this.release(time, chord, downstream);
			}
			this.release(time, single, downstream);
		}
	}

	private void press(long time, LogicalInput input, EventSink<? super LogicalSignal> downstream) {
		var state = new ButtonState(time);
		Long pendingTap = this.pendingTaps.get(input);
		if (pendingTap != null) {
			if (time >= pendingTap && time - pendingTap <= this.timings.doubleTapGapNanos()) {
				state.secondTap = true;
			} else {
				// The gap expired between frames; the earlier tap was a lone one.
				this.pendingTaps.remove(input);
				downstream.accept(new LogicalSignal.Tapped(time, input));
			}
		}
		this.activeButtons.put(input, state);
		downstream.accept(new LogicalSignal.ButtonPress(time, input));
	}

	private void release(long time, LogicalInput input, EventSink<? super LogicalSignal> downstream) {
		ButtonState state = this.activeButtons.remove(input);
		if (state == null) {
			return;
		}
		long duration = time - state.pressedAt;
		boolean isTap = !state.held && duration >= 0 && duration <= this.timings.tapMaxNanos();
		Long previousTap = this.pendingTaps.remove(input);
		boolean isDoubleTap = isTap && state.secondTap && previousTap != null;
		if (previousTap != null && !isDoubleTap) {
			// The previous actuation was a lone tap. Emit it before this release so it stays attributed to that actuation.
			downstream.accept(new LogicalSignal.Tapped(time, input));
		}

		downstream.accept(new LogicalSignal.ButtonRelease(time, input));
		if (isDoubleTap) {
			// Both taps are consumed, so three quick taps form one double tap and a pending tap.
			downstream.accept(new LogicalSignal.DoubleTapped(time, input));
		} else if (isTap) {
			this.pendingTaps.put(input, time);
		}
		if (state.guiPressEligible && duration >= 0) {
			downstream.accept(new LogicalSignal.GuiPress(time, input));
		}
	}

	private void onAxis(long time, Identifier input, float value, EventSink<? super LogicalSignal> downstream) {
		// Domain expansion supplies button edges for axis gestures; raw values only need continuous deltas here.
		float previous = this.axisValues.getOrDefault(input, 0.0f);
		if (value == previous) {
			return;
		}
		if (value == 0.0f) {
			this.axisValues.remove(input);
		} else {
			this.axisValues.put(input, value);
		}
		downstream.accept(new LogicalSignal.AxisMoved(time, new LogicalInput.Single(input), value, value - previous));
	}

	/// @return every chord the trigger forms with held buttons, most specific first
	private List<LogicalInput.ModifiedChord> recogniseChords(Identifier trigger) {
		if (this.pressedButtons.isEmpty()) {
			return List.of();
		}
		List<Identifier> heldByRecency = new ArrayList<>(this.pressedButtons).reversed();
		List<LogicalInput.ModifiedChord> chords = new ArrayList<>();
		int maxModifiers = Math.min(LogicalInput.ModifiedChord.MAX_MODIFIERS, heldByRecency.size());
		for (int size = maxModifiers; size >= 1; size--) {
			collectCombinations(heldByRecency, size, 0, new ArrayList<>(), modifiers ->
					chords.add(new LogicalInput.ModifiedChord(Set.copyOf(modifiers), trigger)));
		}
		return chords;
	}

	/// Emits combinations in lexicographic order of recency, so more recent modifiers come first.
	private static void collectCombinations(
			List<Identifier> items, int size, int from,
			List<Identifier> current, Consumer<List<Identifier>> output
	) {
		if (current.size() == size) {
			output.accept(current);
			return;
		}
		for (int i = from; i <= items.size() - (size - current.size()); i++) {
			current.add(items.get(i));
			collectCombinations(items, size, i + 1, current, output);
			current.removeLast();
		}
	}

	private static final class ButtonState {
		private final long pressedAt;
		private boolean held;
		private boolean guiPressEligible = true;
		/// Pressed within the double tap gap of an earlier tap on the same input.
		private boolean secondTap;

		private ButtonState(long pressedAt) {
			this.pressedAt = pressedAt;
		}
	}
}
