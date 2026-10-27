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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.utils.codec.CExtraCodecs;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	/// Every physical input this logical input is made of.
	Set<Identifier> physicalInputs();

	Codec<LogicalInput> CODEC = CExtraCodecs.fuzzy(
			List.of(Single.CODEC, ModifiedChord.CODEC),
			input -> switch (input) {
				case Single _ -> Single.CODEC;
				case ModifiedChord _ -> ModifiedChord.CODEC;
			}
	);

	/// Codec allowing a null LogicalInput represented by an empty map `{}`
	Codec<Optional<LogicalInput>> OPTIONAL_CODEC = Codec.either(CODEC, CExtraCodecs.EMPTY_MAP).xmap(
			either -> either.left(),
			optional -> optional.isPresent() ? Either.left(optional.get()) : Either.right(Unit.INSTANCE)
	);

	/// A logical input with a direct relationship with a physical input.
	record Single(Identifier physicalInput) implements LogicalInput {
		public static final Codec<Single> CODEC =
				Identifier.CODEC.xmap(Single::new, Single::physicalInput);

		@Override
		public Set<Identifier> physicalInputs() {
			return Set.of(this.physicalInput);
		}
	}

	/// A logical input that triggers only after all modifiers are already pressed.
	/// When several chords match a press, the one with the most modifiers wins.
	record ModifiedChord(Set<Identifier> modifiers, Identifier trigger) implements LogicalInput {
		/// Keeps the number of chords recognised for each press small.
		public static final int MAX_MODIFIERS = 2;

		public static final Codec<ModifiedChord> CODEC = RecordCodecBuilder.<Map.Entry<Set<Identifier>, Identifier>>create(instance -> instance.group(
				CExtraCodecs.set(Identifier.CODEC).fieldOf("modifiers").forGetter(Map.Entry::getKey),
				Identifier.CODEC.fieldOf("trigger").forGetter(Map.Entry::getValue)
		).apply(instance, Map::entry)).flatXmap(
				entry -> validate(entry.getKey(), entry.getValue())
						.<DataResult<ModifiedChord>>map(error -> DataResult.error(() -> error))
						.orElseGet(() -> DataResult.success(new ModifiedChord(entry.getKey(), entry.getValue()))),
				chord -> DataResult.success(Map.entry(chord.modifiers(), chord.trigger()))
		);

		public ModifiedChord {
			modifiers = Set.copyOf(modifiers);
			validate(modifiers, trigger).ifPresent(error -> {
				throw new IllegalArgumentException(error);
			});
		}

		@Override
		public Set<Identifier> physicalInputs() {
			return Stream.concat(this.modifiers.stream(), Stream.of(this.trigger))
					.collect(Collectors.toUnmodifiableSet());
		}

		private static Optional<String> validate(Set<Identifier> modifiers, Identifier trigger) {
			if (modifiers.isEmpty() || modifiers.size() > MAX_MODIFIERS || modifiers.contains(trigger)) {
				return Optional.of("A chord needs 1 to " + MAX_MODIFIERS
						+ " modifiers, none of which is the trigger: " + modifiers + " + " + trigger);
			}
			return Optional.empty();
		}
	}
}
