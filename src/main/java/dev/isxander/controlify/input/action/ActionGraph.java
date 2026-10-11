package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;
import dev.isxander.controlify.utils.event.EventSink;
import net.minecraft.resources.Identifier;

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
			}
		}

		this.currentContexts.clear();
		this.currentContexts.addAll(contexts);
	}

	@Override
	public void accept(LogicalSignal event) {
		switch (event) {
			case LogicalSignal.Tick tick -> this.processTick(tick);
			case LogicalSignal.InputSignal inputSignal -> this.processInput(inputSignal);
			case LogicalSignal.SensorInterval _ -> {} // ignore sensor data
		}
	}

	private void processTick(LogicalSignal.Tick tick) {
		for (ActionImpl action : this.allActions) {
			action.activator().onSignal(tick, action.state());
		}
	}

	private void processInput(LogicalSignal.InputSignal inputSignal) {
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
