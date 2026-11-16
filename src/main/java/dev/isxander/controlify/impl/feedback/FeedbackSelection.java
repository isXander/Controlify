/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.impl.contextual.ContextualState;
import dev.isxander.controlify.impl.contextual.FeedbackRule;
import dev.isxander.controlify.impl.contextual.RuleEngine;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/// Applies feedback rules to contextual state.
public final class FeedbackSelection {
	/// The effects `eventId` plays in `state`, in rule order.
	///
	/// Every rule for the event whose predicate matches plays its effect. A matching rule that
	/// names `controlify:none` stops there: no rule below it plays. If no rule matches at all,
	/// the event plays the effect with its own id.
	public static List<Identifier> select(
			RuleEngine<Identifier, FeedbackRule> rules,
			Identifier eventId,
			ContextualState state
	) {
		return effects(eventId, rules.evaluate(state, rule -> rule.event().equals(eventId), "feedback/" + eventId));
	}

	/// @param matched the event's rules whose predicates matched, in rule order
	static List<Identifier> effects(Identifier eventId, List<FeedbackRule> matched) {
		if (matched.isEmpty()) {
			return List.of(eventId);
		}

		List<Identifier> effects = new ArrayList<>(matched.size());
		for (FeedbackRule rule : matched) {
			if (FeedbackRegistryImpl.NONE.equals(rule.effect())) {
				break;
			}
			effects.add(rule.effect());
		}
		return effects;
	}

	/// Whether any rule is for this event, so state needs calculating to select its effects.
	public static boolean hasRulesFor(RuleEngine<Identifier, FeedbackRule> rules, Identifier eventId) {
		for (FeedbackRule rule : rules.rules()) {
			if (rule.event().equals(eventId)) {
				return true;
			}
		}
		return false;
	}

	private FeedbackSelection() {
	}
}
