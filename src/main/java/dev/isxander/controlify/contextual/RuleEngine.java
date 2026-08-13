/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/// An ordered set of contextual rules for one output type.
///
/// Rules are considered in list order. The first matching rule for a key wins; a rule which does
/// not match does not consume its key, allowing a lower-priority fallback for that key to match.
public final class RuleEngine<K, R extends Rule<K>> {
	private final List<R> rules;
	private final Set<Identifier> factDependencies;

	public RuleEngine(List<? extends R> rules) {
		Objects.requireNonNull(rules, "rules");

		List<R> copiedRules = new ArrayList<>(rules.size());
		Set<Identifier> factDependencies = new LinkedHashSet<>();
		for (R rule : rules) {
			Objects.requireNonNull(rule, "rule");
			Objects.requireNonNull(rule.key(), "rule.key");
			Objects.requireNonNull(rule.predicate(), "rule.predicate");
			copiedRules.add(rule);
			factDependencies.addAll(rule.predicate().factDependencies());
		}

		this.rules = List.copyOf(copiedRules);
		this.factDependencies = Set.copyOf(factDependencies);
	}

	public List<R> rules() {
		return this.rules;
	}

	public Set<Identifier> factDependencies() {
		return this.factDependencies;
	}

	/// Returns the winning rules in their original precedence order.
	public List<R> evaluate(ContextualState state, Predicate<R> shouldEvaluate) {
		Objects.requireNonNull(state, "state");

		List<R> matches = new ArrayList<>();
		Set<K> consumedKeys = new HashSet<>();
		for (R rule : this.rules) {
			if (!shouldEvaluate.test(rule)) {
				continue;
			}

			K key = rule.key();
			if (!consumedKeys.contains(key) && rule.matches(state)) {
				consumedKeys.add(key);
				matches.add(rule);
			}
		}

		return List.copyOf(matches);
	}

	public List<R> evaluate(ContextualState state) {
		return this.evaluate(state, _ -> true);
	}
}
