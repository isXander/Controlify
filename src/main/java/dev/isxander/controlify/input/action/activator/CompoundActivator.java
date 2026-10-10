package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.logical.LogicalSignal;

import java.util.List;
import java.util.stream.Collectors;

public record CompoundActivator(LogicalInput boundInput, List<Activator> activators) implements Activator {

	@Override
	public void onSignal(LogicalSignal signal, Accumulator acc) {
		for (var activator : activators) {
			activator.onSignal(signal, acc);
		}
	}

	@Override
	public String describe() {
		return activators().stream()
				.map(Activator::describe)
				.collect(Collectors.joining(",", "Compound[", "]"));
	}

	public record Config(List<ActivatorConfig<?>> activators) implements ActivatorConfig<CompoundActivator> {
		public static final String TYPE_ID = "compound";
		public static final MapCodec<Config> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ActivatorConfig.CODEC.listOf().fieldOf("activators").forGetter(Config::activators)
		).apply(instance, Config::new));

		@Override
		public CompoundActivator create(LogicalInput boundInput) {
			var activators = this.activators.stream()
					.<Activator>map(config -> config.create(boundInput))
					.toList();
			return new CompoundActivator(boundInput, activators);
		}

		@Override
		public ActivatorConfigType<CompoundActivator, Config> type() {
			return ActivatorConfigType.COMPOUND;
		}
	}
}
