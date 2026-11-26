/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.action.Accumulator;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;

import java.util.List;
import java.util.stream.Collectors;

public record CompoundActivator(List<Activator> activators) implements Activator {

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
		public CompoundActivator create(List<ActionBinding> bindings) {
			var activators = this.activators.stream()
					.<Activator>map(config -> config.create(bindings))
					.toList();
			return new CompoundActivator(activators);
		}

		@Override
		public List<LeafConfig> leaves() {
			return this.activators.stream().flatMap(config -> config.leaves().stream()).toList();
		}

		@Override
		public ActivatorConfigType<CompoundActivator, Config> type() {
			return ActivatorConfigType.COMPOUND;
		}
	}
}
