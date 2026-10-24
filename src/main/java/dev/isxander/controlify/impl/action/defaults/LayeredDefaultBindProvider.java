/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.defaults;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.input.LogicalInput;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public record LayeredDefaultBindProvider(List<Layer> layers) implements DefaultBindProvider {
	public static final Codec<LayeredDefaultBindProvider> CODEC = Codec.list(Layer.CODEC)
			.xmap(LayeredDefaultBindProvider::new, LayeredDefaultBindProvider::layers);

	public static DefaultBindProvider of(Layer... layers) {
		return new LayeredDefaultBindProvider(Arrays.asList(layers));
	}

	@Override
	@Nullable public LogicalInput getDefaultInput(Identifier actionId) {
		for (var layer : layers()) {
			if (layer.provider().hasDefaultInput(actionId)) return layer.provider().getDefaultInput(actionId);
			if (layer.clearBelow()) return null;
		}
		return null;
	}

	@Override
	public boolean hasDefaultInput(Identifier actionId) {
		return this.layers.stream().anyMatch(layer -> layer.provider().hasDefaultInput(actionId) || layer.clearBelow());
	}

	public record Layer(DefaultBindProvider provider, boolean clearBelow) {
		public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MapBackedDefaultBindProvider.MAP_CODEC.forGetter(layer -> (MapBackedDefaultBindProvider) layer.provider()),
				Codec.BOOL.fieldOf("clear_below").forGetter(Layer::clearBelow)
		).apply(instance, Layer::new));
	}
}
