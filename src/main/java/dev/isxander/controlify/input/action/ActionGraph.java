package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;
import dev.isxander.controlify.utils.event.EventSink;
import net.minecraft.resources.Identifier;

import java.util.*;

public class ActionGraph implements EventSink<LogicalSignal> {
	private final Set<ActionImpl> allActions = new TreeSet<>();

	// Context ID -> Logical Input -> set of ActionImpl
	// the same compiled binding can be registered for multiple contexts and inputs
	// this allows for more efficient lookup when processing signals
	// by avoiding the need to iterate over all bindings for each signal
	private final Map<Identifier, Map<LogicalInput, Set<ActionImpl>>> graph = new HashMap<>();

	public void addAction(ActionImpl action) {
		this.allActions.add(action);
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

	}
}
