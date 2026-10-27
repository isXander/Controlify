/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionLayerMode;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.*;

/// Routes logical signals to actions through the stack of active layers.
///
/// Input is resolved from the top layer down: each layer's {@link ActionLayerMode} decides
/// whether an input continues to the layers below it. The result is cached as a dispatch table
/// and only rebuilt when the active layers or bindings change.
///
/// Button gestures belong to the actions that received the press. Actions that become reachable
/// mid-press never see its release, tap or hold; actions that stop being reachable mid-press
/// receive {@link LogicalSignal.Cancelled} instead of the rest of it.
///
/// The pipeline emits every chord a press could form, most specific first, before the single input.
/// The first of those that reaches an action claims its physical buttons until it is released:
/// less specific inputs using a claimed button are suppressed, and presses already holding one,
/// such as the modifier's own single press, are cancelled.
public class ActionDispatcher implements EventSink<LogicalSignal> {
	// use a sorted set to have deterministic ordering
	private final Set<ActionImpl> allActions = new TreeSet<>();

	// Context ID -> Logical Input -> set of ActionImpl
	// the same compiled binding can be registered for multiple contexts and inputs
	private final Map<Identifier, Map<LogicalInput, Set<ActionImpl>>> bindings = new HashMap<>();

	/// Active contexts, top to bottom.
	private List<ActionLayer> currentContexts = List.of();
	/// Logical input -> actions that receive it given the current contexts.
	private Map<LogicalInput, Set<ActionImpl>> dispatch = Map.of();
	private boolean dispatchDirty = true;

	/// Logical input -> actions that received its current press.
	private final Map<LogicalInput, Set<ActionImpl>> pressOwners = new HashMap<>();
	/// Logical input -> actions that owned its last completed press, for its deferred {@link LogicalSignal.Tapped}.
	private final Map<LogicalInput, Set<ActionImpl>> tapOwners = new HashMap<>();
	/// Physical input -> the chord that claimed it.
	private final Map<Identifier, LogicalInput> claims = new HashMap<>();
	private final Map<LogicalInput, LogicalSignal.AxisMoved> continuousInputs = new HashMap<>();
	private long lastSignalNanos;

	public void addAction(ActionImpl action) {
		if (!this.allActions.add(action)) {
			throw new IllegalArgumentException("This action has already been added");
		}
		for (var contextId : action.spec().contexts()) {
			var byInputMap = this.bindings.computeIfAbsent(contextId, _ -> new HashMap<>());
			byInputMap.computeIfAbsent(action.boundInput(), _ -> new TreeSet<>())
					.add(action);
		}
		this.dispatchDirty = true;
	}

	public void removeAction(ActionImpl action) {
		for (var contextId : action.spec().contexts()) {
			var byInputMap = this.bindings.getOrDefault(contextId, Map.of());
			byInputMap.values().removeIf(set -> set.remove(action) && set.isEmpty());
		}
		this.allActions.remove(action);
		this.dispatchDirty = true;
	}

	public void clearActions() {
		this.allActions.clear();
		this.bindings.clear();
		this.claims.clear();
		this.pressOwners.clear();
		this.tapOwners.clear();
		this.dispatch = Map.of();
		this.dispatchDirty = true;
	}

	public void onActionBindingChange(ActionImpl action) {
		// re-indexes the action in the binding index
		this.removeAction(action);
		this.addAction(action);
	}

	/// @param contexts the active contexts, ordered from top to bottom
	public void setCurrentContexts(List<ActionLayer> contexts) {
		if (contexts.equals(this.currentContexts)) {
			return;
		}
		this.currentContexts = List.copyOf(contexts);
		this.dispatchDirty = true;
	}

	/// Releases state from a physical mapping that is being replaced.
	public void resetInputState() {
		this.continuousInputs.clear();
		this.pressOwners.clear();
		this.tapOwners.clear();
		this.claims.clear();
		this.allActions.forEach(ActionImpl::reset);
	}

	/// Resets every action, abandoning presses in progress while keeping held axis values.
	public void resetActions() {
		this.pressOwners.clear();
		this.tapOwners.clear();
		this.claims.clear();
		this.allActions.forEach(ActionImpl::reset);
		this.rebuildDispatchIfDirty();
		this.dispatch.values().forEach(actions -> actions.forEach(this::restoreContinuousState));
	}

	@Override
	public void accept(@NonNull LogicalSignal event) {
		this.lastSignalNanos = event.timeNanos();
		this.rebuildDispatchIfDirty();

		switch (event) {
			case LogicalSignal.Frame frame -> this.processFrame(frame);
			case LogicalSignal.AxisMoved axis -> this.processAxis(axis);
			case LogicalSignal.ButtonPress press -> this.processPress(press);
			case LogicalSignal.InputSignal gesture -> this.processGesture(gesture);
			case LogicalSignal.SensorInterval _ -> {} // ignore sensor data
		}
	}

	private void processFrame(LogicalSignal.Frame frame) {
		for (ActionImpl action : this.allActions) {
			action.activator().onSignal(frame, action.state());
		}
	}

	private void processAxis(LogicalSignal.AxisMoved axis) {
		if (axis.value() == 0f) {
			this.continuousInputs.remove(axis.input());
		} else {
			this.continuousInputs.put(axis.input(), new LogicalSignal.AxisMoved(
					axis.timeNanos(), axis.input(), axis.value(), 0f));
		}
		if (this.isClaimedByOther(axis.input())) {
			return;
		}
		this.deliver(axis, this.dispatch.getOrDefault(axis.input(), Set.of()));
	}

	private void processPress(LogicalSignal.ButtonPress press) {
		LogicalInput input = press.input();
		Set<ActionImpl> targets = this.dispatch.getOrDefault(input, Set.of());
		if (targets.isEmpty() || this.isClaimedByOther(input)) {
			this.pressOwners.remove(input);
			return;
		}
		if (input instanceof LogicalInput.ModifiedChord) {
			this.claim(input);
		}
		this.pressOwners.put(input, new TreeSet<>(targets));
		this.deliver(press, targets);
	}

	private boolean isClaimedByOther(LogicalInput input) {
		for (Identifier physical : input.physicalInputs()) {
			LogicalInput claimer = this.claims.get(physical);
			if (claimer != null && !claimer.equals(input)) {
				return true;
			}
		}
		return false;
	}

	private void claim(LogicalInput chord) {
		Set<Identifier> physical = chord.physicalInputs();
		physical.forEach(input -> this.claims.put(input, chord));

		// The modifiers were pressed first, so their own presses may already be owned.
		List<LogicalInput> overlapping = this.pressOwners.keySet().stream()
				.filter(other -> !other.equals(chord))
				.filter(other -> other.physicalInputs().stream().anyMatch(physical::contains))
				.toList();
		for (LogicalInput other : overlapping) {
			for (ActionImpl owner : this.pressOwners.remove(other)) {
				owner.activator().onSignal(new LogicalSignal.Cancelled(this.lastSignalNanos, other), owner.state());
			}
		}
	}

	/// Release, tap, double tap, hold and GUI press only reach the actions that saw the press.
	private void processGesture(LogicalSignal.InputSignal gesture) {
		LogicalInput input = gesture.input();
		if (gesture instanceof LogicalSignal.Tapped) {
			// Taps arrive after the double tap window, possibly once a newer press has taken ownership.
			Set<ActionImpl> owners = this.tapOwners.remove(input);
			if (owners != null) {
				this.deliver(gesture, owners);
			}
			return;
		}

		Set<ActionImpl> owners = this.pressOwners.get(input);
		if (owners != null) {
			this.deliver(gesture, owners);
		}
		if (gesture instanceof LogicalSignal.ButtonRelease) {
			this.claims.values().removeIf(input::equals);
			if (owners != null) {
				this.tapOwners.put(input, new TreeSet<>(owners));
			} else {
				this.tapOwners.remove(input);
			}
		}
	}

	private void deliver(LogicalSignal signal, Set<ActionImpl> actions) {
		for (ActionImpl action : actions) {
			action.activator().onSignal(signal, action.state());
		}
	}

	private void rebuildDispatchIfDirty() {
		if (!this.dispatchDirty) {
			return;
		}
		this.dispatchDirty = false;

		Map<LogicalInput, Set<ActionImpl>> previous = this.dispatch;
		Map<LogicalInput, Set<ActionImpl>> next = resolveDispatch(this.currentContexts, this.bindings);
		this.dispatch = next;

		// Actions that stopped receiving an input must not be left holding it.
		previous.forEach((input, actions) -> {
			Set<ActionImpl> stillReachable = next.getOrDefault(input, Set.of());
			for (ActionImpl action : actions) {
				if (!stillReachable.contains(action)) {
					this.cancel(action, input);
				}
			}
		});
		this.pressOwners.values().removeIf(owners -> owners.isEmpty());
		this.tapOwners.entrySet().removeIf(entry -> {
			entry.getValue().retainAll(next.getOrDefault(entry.getKey(), Set.of()));
			return entry.getValue().isEmpty();
		});

		// Actions that started receiving an axis pick up its value without replaying presses.
		next.forEach((input, actions) -> {
			Set<ActionImpl> wasReachable = previous.getOrDefault(input, Set.of());
			for (ActionImpl action : actions) {
				if (!wasReachable.contains(action)) {
					this.restoreContinuousState(action);
				}
			}
		});
	}

	private void cancel(ActionImpl action, LogicalInput input) {
		Set<ActionImpl> owners = this.pressOwners.get(input);
		if (owners != null) {
			owners.remove(action);
		}
		// The binding may have changed since the action was reachable, in which case
		// it was already reset and the cancel is a no-op for its new input.
		action.activator().onSignal(new LogicalSignal.Cancelled(this.lastSignalNanos, input), action.state());
	}

	private void restoreContinuousState(ActionImpl action) {
		var value = this.continuousInputs.get(action.boundInput());
		if (value != null) {
			// Restore values without replaying presses, pulses or held-button latches.
			action.activator().onSignal(value, new Accumulator() {
				@Override
				public void setContinuous(float value) {
					action.state().setContinuous(value);
				}
			});
		}
	}

	/// Walks the contexts from top to bottom, collecting the actions each input reaches.
	static Map<LogicalInput, Set<ActionImpl>> resolveDispatch(
			List<ActionLayer> contexts,
			Map<Identifier, Map<LogicalInput, Set<ActionImpl>>> bindings
	) {
		Map<LogicalInput, Set<ActionImpl>> dispatch = new HashMap<>();
		Set<LogicalInput> consumed = new HashSet<>();

		for (ActionLayer context : contexts) {
			Map<LogicalInput, Set<ActionImpl>> byInput = bindings.getOrDefault(context.id(), Map.of());
			byInput.forEach((input, actions) -> {
				if (input != null && !actions.isEmpty() && !consumed.contains(input)) {
					dispatch.computeIfAbsent(input, _ -> new TreeSet<>()).addAll(actions);
				}
			});

			switch (context.mode()) {
				case TRANSPARENT -> {}
				case OVERLAY -> byInput.forEach((input, actions) -> {
					if (input != null && !actions.isEmpty()) {
						consumed.add(input);
					}
				});
				case BLOCKING -> {
					return dispatch;
				}
			}
		}
		return dispatch;
	}
}
