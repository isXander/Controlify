package dev.isxander.controlify.input.physical;

import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import dev.isxander.controlify.controller.input.mapping.MappingEntry;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.UnaryEventStage;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MappingStage implements UnaryEventStage<PhysicalSignal> {
	private Map<Identifier, MappingEntry> mappingByInput;

	public MappingStage(ControllerMapping mapping) {
		this.setControllerMapping(mapping);
	}

	@Override
	public void onEvent(PhysicalSignal event, EventSink<? super PhysicalSignal> downstream) {
		if (event instanceof PhysicalInputSignal inputSignal) {
			MappingEntry mapping = this.mappingByInput.get(inputSignal.input());

			if (mapping != null) {
				downstream.accept(mapping.apply(inputSignal));
				return;
			}
		}
		downstream.accept(event);
	}

	public void setControllerMapping(ControllerMapping mapping) {
		this.mappingByInput = mapping.mappings().stream()
				.collect(Collectors.toUnmodifiableMap(
						MappingEntry::sourceInput,
						Function.identity()
				));
	}
}
