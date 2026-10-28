/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.input;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

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

	/// A logical input with a direct relationship with a physical input.
	record Single(Identifier physicalInput) implements LogicalInput {

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

		@ApiStatus.Internal
		public static Optional<String> validate(Set<Identifier> modifiers, Identifier trigger) {
			if (modifiers.isEmpty() || modifiers.size() > MAX_MODIFIERS || modifiers.contains(trigger)) {
				return Optional.of("A chord needs 1 to " + MAX_MODIFIERS
						+ " modifiers, none of which is the trigger: " + modifiers + " + " + trigger);
			}
			return Optional.empty();
		}
	}
}
