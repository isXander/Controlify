/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.mapping;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.*;

public record ControllerMapping(
		List<MappingEntry> mappings,
		LinkedHashMap<Identifier, DeadzoneGroup> deadzones
) {
	public static final Codec<ControllerMapping> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MappingEntry.codec()
					.listOf()
					.validate(entries -> {
						if (!validateDistinctMappingSources(entries)) {
							return DataResult.error(() -> "Every mapping entry must have a distinct `from` source input");
						}
						return DataResult.success(entries);
					})
					.fieldOf("mappings")
					.forGetter(ControllerMapping::mappings),
			Codec.unboundedMap(Identifier.CODEC, DeadzoneGroup.CODEC)
					.xmap(LinkedHashMap::new, map -> map)
					.validate(groups -> {
						if (!validateDisjointDeadzoneGroups(groups.values())) {
							return DataResult.error(() -> "An axis must not belong to more than one deadzone group");
						}
						return DataResult.success(groups);
					})
					.fieldOf("deadzones")
					.forGetter(ControllerMapping::deadzones)
	).apply(instance, ControllerMapping::new));

	public ControllerMapping {
		if (!validateDistinctMappingSources(mappings)) {
			throw new IllegalArgumentException("Every mapping entry must have a distinct source input");
		}
		if (!validateDisjointDeadzoneGroups(deadzones.values())) {
			throw new IllegalArgumentException("An axis must not belong to more than one deadzone group");
		}
	}

	public static final ControllerMapping NO_MAPPING = new Builder().build();
	public static final ControllerMapping GAMEPAD_DEADZONES_ONLY_MAPPING = new Builder()
			.putDeadzoneGroups(GamepadInputs.DEADZONE_GROUPS)
			.build();

	private static boolean validateDistinctMappingSources(Collection<? extends MappingEntry> entries) {
		long entryCount = entries.size();
		long distinctSourceCount = entries.stream()
				.map(MappingEntry::sourceInput)
				.distinct()
				.count();
		return entryCount == distinctSourceCount;
	}

	private static boolean validateDisjointDeadzoneGroups(Collection<DeadzoneGroup> groups) {
		long axisCount = groups.stream()
				.mapToLong(group -> group.axes().size())
				.sum();
		long distinctAxisCount = groups.stream()
				.flatMap(group -> group.axes().stream())
				.distinct()
				.count();
		return axisCount == distinctAxisCount;
	}

	public static class Builder {
		private final List<MappingEntry> mappings = new ArrayList<>();
		private final LinkedHashMap<Identifier, DeadzoneGroup> deadzones = new LinkedHashMap<>();

		public Builder putMapping(MappingEntry mapping) {
			if (mapping == null)
				return this;

			this.mappings.add(mapping);
			return this;
		}

		public Builder putDeadzoneGroups(Iterable<DeadzoneGroup> deadzoneGroup) {
			for (DeadzoneGroup group : deadzoneGroup) {
				this.deadzones.put(group.name(), group);
			}
			return this;
		}

		public ControllerMapping build() {
			return new ControllerMapping(mappings, deadzones);
		}
	}
}
