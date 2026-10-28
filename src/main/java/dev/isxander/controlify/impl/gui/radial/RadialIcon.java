/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.radial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record RadialIcon(Content content, @Nullable Component overlay) {
	public static final RadialIcon EMPTY = new RadialIcon(new Empty(), null);

	public static final Codec<RadialIcon> CODEC = Definition.CODEC.flatXmap(
			Definition::toRadialIcon,
			RadialIcon::toDefinition
	);

	public static RadialIcon model(Identifier model, boolean enchantGlint) {
		return new RadialIcon(new Model(model, enchantGlint), null);
	}

	public static RadialIcon model(Identifier model) {
		return model(model, false);
	}

	public static RadialIcon texture(Identifier texture) {
		return new RadialIcon(new Texture(texture), null);
	}

	public RadialIcon withOverlay(Component overlay) {
		return new RadialIcon(content, overlay);
	}

	private static DataResult<Definition> toDefinition(RadialIcon icon) {
		return switch (icon.content()) {
			case Model model -> DataResult.success(new Definition(
					Optional.of(model),
					Optional.empty(),
					Optional.ofNullable(icon.overlay())
			));
			case Texture texture -> DataResult.success(new Definition(
					Optional.empty(),
					Optional.of(texture),
					Optional.ofNullable(icon.overlay())
			));
			default -> DataResult.error(() -> "Only model and texture radial icons can be serialized");
		};
	}

	public sealed interface Content permits Empty, Model, Texture {
	}

	public record Empty() implements Content {
	}

	public record Model(Identifier id, boolean enchantGlint) implements Content {
		public static final Codec<Model> CODEC = RecordCodecBuilder.<Model>create(instance -> instance.group(
				Identifier.CODEC.fieldOf("id").forGetter(Model::id),
				Codec.BOOL.optionalFieldOf("enchant_glint", false).forGetter(Model::enchantGlint)
		).apply(instance, Model::new))
				.withAlternative(Identifier.CODEC.xmap(id -> new Model(id, false), Model::id));
	}

	public record Texture(Identifier id) implements Content {
		public static final Codec<Texture> CODEC = Identifier.CODEC.xmap(Texture::new, Texture::id);
	}

	private record Definition(Optional<Model> model, Optional<Texture> texture, Optional<Component> overlay) {
		private static final Codec<Definition> CODEC = RecordCodecBuilder.<Definition>create(instance -> instance.group(
				Model.CODEC.optionalFieldOf("model").forGetter(Definition::model),
				Texture.CODEC.optionalFieldOf("texture").forGetter(Definition::texture),
				ComponentSerialization.CODEC.optionalFieldOf("overlay").forGetter(Definition::overlay)
		).apply(instance, Definition::new))
				.validate(def -> {
					if (def.model.isPresent() == def.texture.isPresent()) {
						return DataResult.error(() -> "Radial icon must define exactly one of 'model' or 'texture'");
					}
					return DataResult.success(def);
				});

		private DataResult<RadialIcon> toRadialIcon() {
			if (this.model.isPresent() == this.texture.isPresent()) {
				return DataResult.error(() -> "Radial icon must define exactly one of 'model' or 'texture'");
			}

			Content content = this.model.isPresent()
					? this.model.get()
					: this.texture.get();

			return DataResult.success(new RadialIcon(content, this.overlay.orElse(null)));
		}
	}
}
