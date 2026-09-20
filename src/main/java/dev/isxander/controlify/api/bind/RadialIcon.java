/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.bind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record RadialIcon(Content content, @Nullable Component overlay) {
	public static final RadialIcon EMPTY = new RadialIcon(new Empty(), null);

	public static final Codec<RadialIcon> CODEC = Definition.CODEC.flatXmap(
			Definition::toRadialIcon,
			RadialIcon::toDefinition
	);

	public static RadialIcon model(Identifier model) {
		return new RadialIcon(new Model(model), null);
	}

	public static RadialIcon texture(Identifier texture) {
		return new RadialIcon(new Texture(texture), null);
	}

	public RadialIcon withOverlay(Component overlay) {
		return new RadialIcon(content, overlay);
	}

	private static DataResult<Definition> toDefinition(RadialIcon icon) {
		return switch (icon.content()) {
			case Model(Identifier model) -> DataResult.success(new Definition(Optional.of(model), Optional.empty()));
			case Texture(Identifier texture) -> DataResult.success(new Definition(Optional.empty(), Optional.of(texture)));
			default -> DataResult.error(() -> "Only model and texture radial icons can be serialized");
		};
	}

	public sealed interface Content permits Empty, Model, Texture {
	}

	public record Empty() implements Content {
	}

	public record Model(Identifier id) implements Content {
	}

	public record Texture(Identifier id) implements Content {
	}

	private record Definition(Optional<Identifier> model, Optional<Identifier> texture) {
		private static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.optionalFieldOf("model").forGetter(Definition::model),
				Identifier.CODEC.optionalFieldOf("texture").forGetter(Definition::texture)
		).apply(instance, Definition::new));

		private DataResult<RadialIcon> toRadialIcon() {
			if (model.isPresent() == texture.isPresent()) {
				return DataResult.error(() -> "Radial icon must define exactly one of 'model' or 'texture'");
			}

			return DataResult.success(model
					.map(RadialIcon::model)
					.orElseGet(() -> RadialIcon.texture(texture.orElseThrow())));
		}
	}
}
