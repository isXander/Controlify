/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.utils.codec.CExtraCodecs;

import java.util.List;
import java.util.Optional;

/// One input bound to an action.
/// @param input the input that activates the action
/// @param gesture overrides the gesture of the action's editable activator for this binding, if it has one
///                of the same {@link Gesture.Kind}; ignored otherwise
public record ActionBinding(LogicalInput input, Optional<Gesture> gesture) {
	private static final Codec<ActionBinding> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LogicalInput.CODEC.fieldOf("input").forGetter(ActionBinding::input),
			Gesture.CODEC.optionalFieldOf("gesture").forGetter(ActionBinding::gesture)
	).apply(instance, ActionBinding::new));

	/// A bare input, or `{"input": ..., "gesture": ...}`.
	public static final Codec<ActionBinding> CODEC = Codec.either(LogicalInput.CODEC, FULL_CODEC).xmap(
			either -> either.map(ActionBinding::of, binding -> binding),
			binding -> binding.gesture().isEmpty() ? Either.left(binding.input()) : Either.right(binding)
	);

	public ActionBinding(LogicalInput input) {
		this(input, Optional.empty());
	}

	public static ActionBinding of(LogicalInput input) {
		return new ActionBinding(input);
	}

	public ActionBinding withGesture(Optional<Gesture> gesture) {
		return new ActionBinding(this.input, gesture);
	}
}
