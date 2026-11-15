/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/// A rule that decides which feedback effect plays. There are two kinds:
///
/// - `{"for": <event id>, "if": <predicate>, "then": <effect id>}` chooses the effect an
///   event plays when code fires or holds it. `controlify:none` plays nothing.
/// - `{"if": <predicate>, "then": <effect id>}`, with no `for`, needs no event at all: the
///   effect is held for as long as the predicate is true.
///
/// @param event  the event this rule selects an effect for, or empty for a state-driven rule
/// @param effect the id of the effect to play
public record FeedbackRule(
		Optional<Identifier> event,
		ContextualPredicate predicate,
		Identifier effect
) implements Rule<Identifier> {
	public static final Codec<FeedbackRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("for").forGetter(FeedbackRule::event),
			ContextualPredicate.CODEC.fieldOf("if").forGetter(FeedbackRule::predicate),
			Identifier.CODEC.fieldOf("then").forGetter(FeedbackRule::effect)
	).apply(instance, FeedbackRule::new));

	public FeedbackRule(Identifier event, ContextualPredicate predicate, Identifier effect) {
		this(Optional.of(event), predicate, effect);
	}

	/// Whether this rule plays its effect from contextual state alone, with no event.
	public boolean isStateDriven() {
		return this.event.isEmpty();
	}

	/// Rules for one event compete, so the first match wins. State-driven rules compete only
	/// with other rules for the same effect, so different effects can play together.
	@Override
	public Identifier key() {
		return this.event.orElse(this.effect);
	}
}
