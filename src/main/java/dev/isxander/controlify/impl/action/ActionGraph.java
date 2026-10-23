/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ActionGraph implements EventSink<LogicalSignal> {
	// use a sorted set to have deterministic ordering
	private final Set<ActionImpl> allActions = new TreeSet<>();

	// Context ID -> Logical Input -> set of ActionImpl
	// the same compiled binding can be registered for multiple contexts and inputs
	// this allows for more efficient lookup when processing signals
	// by avoiding the need to iterate over all bindings for each signal
	private final Map<Identifier, Map<LogicalInput, Set<ActionImpl>>> graph = new HashMap<>();

	private final Set<ActionContext> currentContexts = new TreeSet<>();
	private final Map<LogicalInput, LogicalSignal.AxisMoved> continuousInputs = new HashMap<>();
	// mutable set to avoid set allocation on every signal
	private final Set<ActionImpl> dispatchCandidates = new TreeSet<>();

	public void addAction(ActionImpl action) {
		if (!this.allActions.add(action)) {
			throw new IllegalArgumentException("This action has already been added");
		}
		for (var contextId : action.spec().contexts()) {
			var byInputMap = this.graph.computeIfAbsent(contextId, _ -> new HashMap<>());
			byInputMap.computeIfAbsent(action.boundInput(), _ -> new TreeSet<>())
					.add(action);
		}
		this.restoreContinuousState(action);
	}

	public void removeAction(ActionImpl action) {
		for (var contextId : action.spec().contexts()) {
			var byInputMap = this.graph.getOrDefault(contextId, Map.of());
			for (var set : byInputMap.values()) {
				set.remove(action);
			}
		}
		this.allActions.remove(action);
	}

	public void clearActions() {
		this.allActions.clear();
		this.graph.clear();
	}

	public void onActionBindingChange(ActionImpl action) {
		// re-indexes the action in the graph
		this.removeAction(action);
		this.addAction(action);
	}

	public void setCurrentContexts(Set<ActionContext> contexts) {
		// Reset only actions that lose their last active context.
		for (ActionImpl action : this.allActions) {
			boolean wasActive = this.currentContexts.stream()
					.anyMatch(context -> action.spec().contexts().contains(context.id()));
			boolean isActive = contexts.stream()
					.anyMatch(context -> action.spec().contexts().contains(context.id()));
			if (wasActive && !isActive) {
				action.reset();
			} else if (!wasActive && isActive) {
				this.restoreContinuousState(action, true);
			}
		}

		this.currentContexts.clear();
		this.currentContexts.addAll(contexts);
	}

	/// Releases state from a physical mapping that is being replaced.
	public void resetInputState() {
		this.continuousInputs.clear();
		this.allActions.forEach(ActionImpl::reset);
	}

	public void restoreContinuousState() {
		this.allActions.forEach(this::restoreContinuousState);
	}

	private void restoreContinuousState(ActionImpl action) {
		this.restoreContinuousState(action, this.currentContexts.stream()
				.anyMatch(context -> action.spec().contexts().contains(context.id())));
	}

	private void restoreContinuousState(ActionImpl action, boolean active) {
		var value = this.continuousInputs.get(action.boundInput());
		if (active && value != null) {
			// Restore values without replaying presses, pulses or held-button latches.
			action.activator().onSignal(value, new Accumulator() {
				@Override
				public void setContinuous(float value) {
					action.state().setContinuous(value);
				}
			});
		}
	}

	@Override
	public void accept(@NonNull LogicalSignal event) {
		switch (event) {
			case LogicalSignal.Frame frame -> this.processFrame(frame);
			case LogicalSignal.InputSignal inputSignal -> this.processInput(inputSignal);
			case LogicalSignal.SensorInterval _ -> {} // ignore sensor data
		}
	}

	private void processFrame(LogicalSignal.Frame frame) {
		for (ActionImpl action : this.allActions) {
			action.activator().onSignal(frame, action.state());
		}
	}

	private void processInput(LogicalSignal.InputSignal inputSignal) {
		if (inputSignal instanceof LogicalSignal.AxisMoved axis) {
			if (axis.value() == 0f) {
				this.continuousInputs.remove(axis.input());
			} else {
				this.continuousInputs.put(axis.input(), new LogicalSignal.AxisMoved(
						axis.timeNanos(), axis.input(), axis.value(), 0f));
			}
		}
		this.dispatchCandidates.clear();

		// collect all actions monitoring this logical input in the current context
		// actions in multiple contexts are automatically deduplicated by the set
		for (ActionContext context : this.currentContexts) {
			Map<LogicalInput, Set<ActionImpl>> byInput = this.graph.get(context.id());

			if (byInput == null) {
				continue;
			}

			Set<ActionImpl> actionSet = byInput.get(inputSignal.input());

			if (actionSet == null) {
				continue;
			}

			this.dispatchCandidates.addAll(actionSet);
		}

		for (ActionImpl action : this.dispatchCandidates) {
			action.activator().onSignal(inputSignal, action.state());
		}
	}
}
