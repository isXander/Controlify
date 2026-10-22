/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.input;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.utils.codec.CExtraCodecs;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/// A reference to a logical button or axis.
///
/// A logical input's domain is the type of input, meaning that
/// a logical input that refers to the same physical inputs can be
/// safely duplicated between continuous and digital signals.
///
/// Most of the time this will refer to a single physical button or axis,
/// like the `A` button or `Left Stick` left, but it may also refer to
/// a chord, like `A` + `B`.
public sealed interface LogicalInput {
	Codec<LogicalInput> CODEC = CExtraCodecs.fuzzy(
			List.of(Single.CODEC, ModifiedChord.CODEC),
			input -> switch (input) {
				case Single _ -> Single.CODEC;
				case ModifiedChord _ -> ModifiedChord.CODEC;
			}
	);

	/// Codec allowing a null LogicalInput represented by an empty map `{}`
	Codec<Optional<LogicalInput>> OPTIONAL_CODEC = Codec.either(CODEC, MapCodec.unitCodec(() -> Unit.INSTANCE)).xmap(
			either -> either.left(),
			optional -> optional.isPresent() ? Either.left(optional.get()) : Either.right(Unit.INSTANCE)
	);

	/// A logical input with a direct relationship with a physical input.
	record Single(Identifier physicalInput) implements LogicalInput {
		public static final Codec<Single> CODEC =
				Identifier.CODEC.xmap(Single::new, Single::physicalInput);
	}

	/// A logical input that triggers only after all modifiers are already pressed.
	record ModifiedChord(Set<Identifier> modifiers, Identifier trigger) implements LogicalInput {
		public static final Codec<ModifiedChord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				CExtraCodecs.set(Identifier.CODEC).fieldOf("modifiers").forGetter(ModifiedChord::modifiers),
				Identifier.CODEC.fieldOf("trigger").forGetter(ModifiedChord::trigger)
		).apply(instance, ModifiedChord::new));
	}
}
