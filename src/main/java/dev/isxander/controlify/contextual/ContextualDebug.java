/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

final class ContextualDebug {
	private static final Comparator<Identifier> IDENTIFIER_ORDER = Comparator.comparing(Identifier::toString);

	private ContextualDebug() {
	}

	static boolean enabled() {
		return DebugProperties.DEBUG_CONTEXTUAL_RULES;
	}

	static void logStateStart(
			Identifier domain,
			ContextualState state,
			@Nullable Collection<Identifier> requiredFacts
	) {
		if (!enabled()) {
			return;
		}

		CUtil.LOGGER.log(
				"[Contextual/{}] Resolving state\n  required facts: {}\n  source facts: {}\n  item slots: {}\n  block slots: {}\n  entity slots: {}",
				domain,
				describeIdentifiers(requiredFacts),
				describeMap(state.facts(), String::valueOf),
				describeMap(state.items(), ContextualDebug::describeItem),
				describeMap(state.blocks(), ContextualDebug::describeBlock),
				describeMap(state.entities(), ContextualDebug::describeEntity)
		);
	}

	static void logStateComplete(Identifier domain, ContextualState state) {
		if (!enabled()) {
			return;
		}

		CUtil.LOGGER.log(
				"[Contextual/{}] Resolved state\n  facts: {}",
				domain,
				describeMap(state.facts(), String::valueOf)
		);
	}

	static PredicateEvaluation evaluatePredicate(ContextualPredicate predicate, ContextualState state) {
		var trace = new StringBuilder();
		boolean result = evaluatePredicate(predicate, state, trace, 0);
		return new PredicateEvaluation(result, trace.toString());
	}

	static void logFact(
			Identifier domain,
			Identifier fact,
			Set<Identifier> dependencies,
			PredicateEvaluation evaluation
	) {
		if (!enabled()) {
			return;
		}

		CUtil.LOGGER.log(
				"[Contextual/{}] Fact '{}' = {}\n  dependencies: {}\n{}",
				domain,
				fact,
				evaluation.result(),
				describeIdentifiers(dependencies),
				evaluation.trace()
		);
	}

	static void logRule(
			@Nullable String context,
			int index,
			Object key,
			ContextualPredicate predicate,
			ContextualState state,
			String outcome,
			@Nullable PredicateEvaluation evaluation
	) {
		if (!enabled() || context == null) {
			return;
		}

		String trace = evaluation == null ? "  predicate was not evaluated" : evaluation.trace();
		CUtil.LOGGER.log(
				"[Contextual/{}] Rule #{} key={} -> {}\n  contributing facts: {}\n  predicate: {}\n{}",
				context,
				index,
				key,
				outcome,
				describeFactValues(predicate.factDependencies(), state),
				predicate,
				trace
		);
	}

	static void logRuleResult(@Nullable String context, Collection<?> matches) {
		if (enabled() && context != null) {
			CUtil.LOGGER.log("[Contextual/{}] Winning rules: {}", context, matches);
		}
	}

	private static boolean evaluatePredicate(
			ContextualPredicate predicate,
			ContextualState state,
			StringBuilder trace,
			int depth
	) {
		int start = trace.length();
		String indent = "  ".repeat(depth + 1);
		boolean result;

		if (predicate instanceof ContextualPredicate.Compound compound) {
			boolean allOf = evaluateGroup("all_of", compound.allOf().orElse(Set.of()), state, trace, depth + 1, Group.ALL);
			boolean noneOf = evaluateGroup("none_of", compound.noneOf().orElse(Set.of()), state, trace, depth + 1, Group.NONE);
			boolean anyOf = compound.anyOf().isEmpty()
					|| evaluateGroup("any_of", compound.anyOf().get(), state, trace, depth + 1, Group.ANY);
			result = allOf && noneOf && anyOf;
			trace.insert(start, indent + "compound -> " + result + '\n');
			return result;
		}

		result = predicate.matches(state);
		String detail = switch (predicate) {
			case ContextualPredicate.Fact fact -> "fact " + fact.fact() + "=" + state.facts().getOrDefault(fact.fact(), false);
			case ContextualPredicate.Item item -> "item slot " + item.slot() + "=" + describeItem(state.items().get(item.slot()))
					+ ", predicate=" + item.item();
			case ContextualPredicate.Block block -> "block slot " + block.slot() + "=" + describeBlock(state.blocks().get(block.slot()))
					+ ", predicate=" + block.block();
			case ContextualPredicate.Entity entity -> "entity slot " + entity.slot() + "=" + describeEntity(state.entities().get(entity.slot()))
					+ ", predicate=" + entity.entity();
			case ContextualPredicate.Static staticPredicate -> "static " + staticPredicate.value();
			case ContextualPredicate.Compound _ -> throw new AssertionError();
		};
		trace.append(indent).append(detail).append(" -> ").append(result).append('\n');
		return result;
	}

	private static boolean evaluateGroup(
			String name,
			Set<ContextualPredicate> predicates,
			ContextualState state,
			StringBuilder trace,
			int depth,
			Group group
	) {
		if (predicates.isEmpty()) {
			return true;
		}

		String indent = "  ".repeat(depth + 1);
		trace.append(indent).append(name).append(':').append('\n');
		boolean any = false;
		boolean all = true;
		for (ContextualPredicate child : predicates) {
			boolean childResult = evaluatePredicate(child, state, trace, depth + 1);
			any |= childResult;
			all &= childResult;
		}

		return switch (group) {
			case ALL -> all;
			case NONE -> !any;
			case ANY -> any;
		};
	}

	private static String describeFactValues(Set<Identifier> facts, ContextualState state) {
		var values = new StringJoiner(", ", "{", "}");
		facts.stream().sorted(IDENTIFIER_ORDER).forEach(id ->
				values.add(id + "=" + state.facts().getOrDefault(id, false))
		);
		return values.toString();
	}

	private static String describeIdentifiers(@Nullable Collection<Identifier> identifiers) {
		if (identifiers == null) {
			return "<all>";
		}

		var result = new StringJoiner(", ", "[", "]");
		identifiers.stream().sorted(IDENTIFIER_ORDER).forEach(id -> result.add(id.toString()));
		return result.toString();
	}

	private static <T> String describeMap(Map<Identifier, T> map, java.util.function.Function<T, String> describer) {
		var result = new StringJoiner(", ", "{", "}");
		map.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(IDENTIFIER_ORDER))
				.forEach(entry -> result.add(entry.getKey() + "=" + describer.apply(entry.getValue())));
		return result.toString();
	}

	private static String describeItem(@Nullable ItemInstance item) {
		return item == null ? "<missing>" : item.toString();
	}

	private static String describeBlock(@Nullable BlockInWorld block) {
		return block == null ? "<missing>" : block.getState() + " at " + block.getPos();
	}

	private static String describeEntity(@Nullable Entity entity) {
		return entity == null
				? "<missing>"
				: entity.getType() + "#" + entity.getId() + " ('" + entity.getDisplayName().getString() + "') at " + entity.position();
	}

	record PredicateEvaluation(boolean result, String trace) {
	}

	private enum Group {
		ALL,
		NONE,
		ANY
	}
}
