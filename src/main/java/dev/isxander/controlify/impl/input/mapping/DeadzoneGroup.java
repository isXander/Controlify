/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.mapping;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/// A set of axes that share one radial deadzone.
/// Each dimension is a signed physical axis split into two mutually exclusive half-axes.
public record DeadzoneGroup(
		Identifier name,
		List<Dimension> dimensions
) {
	public static final Codec<DeadzoneGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("name").forGetter(DeadzoneGroup::name),
			Dimension.CODEC.listOf().fieldOf("dimensions").forGetter(DeadzoneGroup::dimensions)
	).apply(instance, DeadzoneGroup::new));

	public DeadzoneGroup {
		dimensions = List.copyOf(dimensions);
		List<Identifier> axes = axesOf(dimensions);
		if (axes.size() != axes.stream().distinct().count()) {
			throw new IllegalArgumentException("Deadzone group " + name + " contains an axis more than once");
		}
	}

	/// Every half-axis in this group, in dimension order: positive then negative for each dimension.
	public List<Identifier> axes() {
		return axesOf(this.dimensions);
	}

	private static List<Identifier> axesOf(List<Dimension> dimensions) {
		return dimensions.stream()
				.<Identifier>mapMulti((dimension, sink) -> {
					sink.accept(dimension.positive());
					dimension.negative().ifPresent(sink);
				})
				.toList();
	}

	/// @param positive the half-axis for the positive direction
	/// @param negative the opposing half-axis, if this dimension is signed
	public record Dimension(Identifier positive, Optional<Identifier> negative) {
		public static final Codec<Dimension> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("positive").forGetter(Dimension::positive),
				Identifier.CODEC.optionalFieldOf("negative").forGetter(Dimension::negative)
		).apply(instance, Dimension::new));

		public Dimension(Identifier positive, Identifier negative) {
			this(positive, Optional.of(negative));
		}
	}
}
