package dev.isxander.controlify.input.logical;

import dev.isxander.controlify.input.physical.PhysicalInputSignal;
import dev.isxander.controlify.input.physical.PhysicalSensorSignal;
import dev.isxander.controlify.input.physical.PhysicalSignal;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.EventStage;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/// Converts physical inputs into single inputs and arbitrary modifier chords.
/// Each trigger uses the most recently pressed button still held as its sole
/// modifier. The chord keeps that modifier until either button is released,
/// and follows the same gesture lifecycle as a single button.
///
/// Button gestures use event timestamps, with holds emitted once on a tick.
/// Call {@link #onGuiNavigate()} when GUI navigation consumes a pending press.
/// Sensor integration is delegated to {@link SensorAccumulatorStage}.
public class PhysicalToLogicalStage implements EventStage<PhysicalSignal, LogicalSignal> {
	public static final long DEFAULT_TAP_MAX_NANOS = 200_000_000L;
	public static final long DEFAULT_DOUBLE_TAP_MAX_NANOS = 300_000_000L;
	public static final long DEFAULT_HOLD_NANOS = 500_000_000L;

	private final SensorAccumulatorStage sensorAccumulatorStage;
	private final long tapMaxNanos, doubleTapMaxNanos, holdNanos;

	// Press order lets an older held button become the latest modifier again when a newer one is released.
	private final LinkedHashSet<Identifier> pressedButtons = new LinkedHashSet<>();
	private final Map<LogicalInput, ButtonState> activeButtons = new LinkedHashMap<>();
	private final Map<LogicalInput, Long> lastTaps = new HashMap<>();
	private final Map<Identifier, Float> axisValues = new HashMap<>();

	public PhysicalToLogicalStage(SensorAccumulatorStage sensorAccumulatorStage) {
		this(sensorAccumulatorStage, DEFAULT_TAP_MAX_NANOS,
				DEFAULT_DOUBLE_TAP_MAX_NANOS, DEFAULT_HOLD_NANOS);
	}

	public PhysicalToLogicalStage(
			SensorAccumulatorStage sensorAccumulatorStage,
			long tapMaxNanos, long doubleTapMaxNanos, long holdNanos
	) {
		this.sensorAccumulatorStage = Objects.requireNonNull(sensorAccumulatorStage);
		if (tapMaxNanos < 0 || doubleTapMaxNanos < 0 || holdNanos <= tapMaxNanos) {
			throw new IllegalArgumentException("Gesture durations must be nonnegative and hold must exceed tap");
		}
		this.tapMaxNanos = tapMaxNanos;
		this.doubleTapMaxNanos = doubleTapMaxNanos;
		this.holdNanos = holdNanos;
	}

	/// Cancels GUI presses for buttons already held; subsequent presses remain eligible.
	public void onGuiNavigate() {
		this.activeButtons.values().forEach(state -> state.guiPressEligible = false);
	}

	@Override
	public void onEvent(PhysicalSignal event, EventSink<? super LogicalSignal> downstream) {
		switch (event) {
			case PhysicalSignal.Tick(long timeNanos, long deltaTimeNanos) -> {
				this.sensorAccumulatorStage.onEvent(event, downstream);
				// Holds must fire even without new input events, before downstream actions process this frame's tick.
				this.activeButtons.forEach((input, state) -> {
					if (!state.held && timeNanos - state.pressedAt >= this.holdNanos) {
						state.held = true;
						downstream.accept(new LogicalSignal.Held(timeNanos, input));
					}
				});
				this.lastTaps.values().removeIf(time -> timeNanos - time > this.doubleTapMaxNanos);
				downstream.accept(new LogicalSignal.Tick(timeNanos, deltaTimeNanos));
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
			LogicalInput.ModifiedChord chord = this.recogniseChord(input);
			this.press(time, single, downstream);
			if (chord != null) {
				this.press(time, chord, downstream);
			}
			this.pressedButtons.add(input);
		} else {
			this.pressedButtons.remove(input);
			this.release(time, single, downstream);
			// Use the chords captured on press, so later presses cannot change which chord gets released.
			// Snapshot because releasing a chord removes it from activeButtons.
			for (var logicalInput : this.activeButtons.keySet().stream().toList()) {
				if (logicalInput instanceof LogicalInput.ModifiedChord chord
						&& (chord.trigger().equals(input) || chord.modifiers().contains(input))) {
					this.release(time, chord, downstream);
				}
			}
		}
	}

	private void press(long time, LogicalInput input, EventSink<? super LogicalSignal> downstream) {
		this.activeButtons.put(input, new ButtonState(time));
		downstream.accept(new LogicalSignal.ButtonPress(time, input));
	}

	private void release(long time, LogicalInput input, EventSink<? super LogicalSignal> downstream) {
		ButtonState state = this.activeButtons.remove(input);
		if (state == null) {
			return;
		}
		downstream.accept(new LogicalSignal.ButtonRelease(time, input));
		long duration = time - state.pressedAt;
		if (!state.held && duration >= 0 && duration <= this.tapMaxNanos) {
			downstream.accept(new LogicalSignal.Tapped(time, input));
			// Consume the first tap so three quick taps form one double tap, rather than overlapping pairs.
			Long previousTap = this.lastTaps.remove(input);
			if (previousTap != null && time >= previousTap && time - previousTap <= this.doubleTapMaxNanos) {
				downstream.accept(new LogicalSignal.DoubleTapped(time, input));
			} else {
				this.lastTaps.put(input, time);
			}
		} else {
			this.lastTaps.remove(input);
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

	private LogicalInput.ModifiedChord recogniseChord(Identifier trigger) {
		if (!this.pressedButtons.isEmpty()) {
			return new LogicalInput.ModifiedChord(Set.of(this.pressedButtons.getLast()), trigger);
		}
		return null;
	}

	private static final class ButtonState {
		private final long pressedAt;
		private boolean held;
		private boolean guiPressEligible = true;

		private ButtonState(long pressedAt) {
			this.pressedAt = pressedAt;
		}
	}
}
