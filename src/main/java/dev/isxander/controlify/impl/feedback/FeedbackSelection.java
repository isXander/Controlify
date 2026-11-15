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
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Applies feedback rules to contextual state.
public final class FeedbackSelection {
	/// @return the effect named by the first rule for `eventId` whose predicate matches `state`,
	///         or null if none matched
	public static @Nullable Identifier select(
			RuleEngine<Identifier, FeedbackRule> rules,
			Identifier eventId,
			ContextualState state
	) {
		List<FeedbackRule> matched = rules.evaluate(
				state,
				rule -> rule.event().isPresent() && rule.event().get().equals(eventId),
				"feedback/" + eventId
		);
		return matched.isEmpty() ? null : matched.getFirst().effect();
	}

	/// @return the effects of every state-driven rule (one with no `for`) whose predicate
	///         matches `state`, each at most once
	public static List<Identifier> active(RuleEngine<Identifier, FeedbackRule> rules, ContextualState state) {
		return rules.evaluate(state, FeedbackRule::isStateDriven, "feedback/state").stream()
				.map(FeedbackRule::effect)
				.toList();
	}

	/// Whether any rule is state-driven, so state needs evaluating every tick.
	public static boolean hasStateDriven(RuleEngine<Identifier, FeedbackRule> rules) {
		for (FeedbackRule rule : rules.rules()) {
			if (rule.isStateDriven()) {
				return true;
			}
		}
		return false;
	}

	private FeedbackSelection() {
	}
}
