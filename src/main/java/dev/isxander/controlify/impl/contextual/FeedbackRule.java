/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import net.minecraft.resources.Identifier;

/// A rule that plays a feedback effect for an event while a predicate is true:
/// `{"for": <event id>, "if": <predicate>, "then": <effect id>}`.
///
/// Every matching rule for an event plays, so one event can play several effects at once.
/// `"then": "controlify:none"` stops the rules below it for that event.
///
/// `for` defaults to `controlify:ambient`, which is held for as long as the player is in a
/// world, so a rule without `for` plays its effect whenever its predicate is true.
///
/// @param event  the event this rule plays an effect for
/// @param effect the id of the effect to play
public record FeedbackRule(
		Identifier event,
		ContextualPredicate predicate,
		Identifier effect
) implements Rule<Identifier> {
	public static final Codec<FeedbackRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("for").forGetter(FeedbackRule::event),
			ContextualPredicate.CODEC.fieldOf("if").forGetter(FeedbackRule::predicate),
			Identifier.CODEC.fieldOf("then").forGetter(FeedbackRule::effect)
	).apply(instance, FeedbackRule::new));

	/// Rules are only ever evaluated for one event at a time, so the effect alone identifies a
	/// rule: two matching rules for the same event and effect play it once.
	@Override
	public Identifier key() {
		return this.effect;
	}
}
