/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/// A compiled evaluation plan for data-defined facts.
///
/// References to other data-defined facts are graph edges. References which do not have a
/// definition are treated as source facts and are expected to be supplied by contributors.
public final class FactDependencyGraph {
	private static final Comparator<Identifier> IDENTIFIER_ORDER = Comparator.comparing(Identifier::toString);
	private static final FactDependencyGraph EMPTY = new FactDependencyGraph(Map.of(), Map.of(), List.of(), Set.of());

	private final Map<Identifier, FactDefinition> definitions;
	private final Map<Identifier, Set<Identifier>> dependencies;
	private final List<Identifier> evaluationOrder;
	private final Set<Identifier> externalDependencies;
	private final Map<Set<Identifier>, List<Identifier>> evaluationPlans = new ConcurrentHashMap<>();

	private FactDependencyGraph(
			Map<Identifier, FactDefinition> definitions,
			Map<Identifier, Set<Identifier>> dependencies,
			List<Identifier> evaluationOrder,
			Set<Identifier> externalDependencies
	) {
		this.definitions = definitions;
		this.dependencies = dependencies;
		this.evaluationOrder = evaluationOrder;
		this.externalDependencies = externalDependencies;
	}

	public static FactDependencyGraph empty() {
		return EMPTY;
	}

	/// Compiles fact definitions into a deterministic dependency-first evaluation order.
	///
	/// @throws IllegalArgumentException if a definition id is duplicated
	/// @throws CyclicFactDependencyException if the definitions contain a dependency cycle
	public static FactDependencyGraph compile(Collection<FactDefinition> factDefinitions) {
		Objects.requireNonNull(factDefinitions, "factDefinitions");

		// put all definitions in a map
		Map<Identifier, FactDefinition> definitions = new LinkedHashMap<>();
		for (FactDefinition definition : factDefinitions) {
			Objects.requireNonNull(definition, "factDefinition");
			Objects.requireNonNull(definition.id(), "factDefinition.id");
			Objects.requireNonNull(definition.predicate(), "factDefinition.predicate");
			if (definitions.putIfAbsent(definition.id(), definition) != null) {
				throw new IllegalArgumentException("Duplicate contextual fact definition: " + definition.id());
			}
		}

		// short circuit to empty instance
		if (definitions.isEmpty()) {
			return empty();
		}

		// get dependencies of each fact in a stable order
		Map<Identifier, Set<Identifier>> dependencies = new LinkedHashMap<>();
		for (FactDefinition definition : definitions.values()) {
			List<Identifier> sortedDependencies = new ArrayList<>(definition.predicate().factDependencies());
			sortedDependencies.sort(IDENTIFIER_ORDER); // ensure stable order
			dependencies.put(definition.id(), Collections.unmodifiableSet(new LinkedHashSet<>(sortedDependencies)));
		}

		// compile evaluation order for dependencies
		Compiler compiler = new Compiler(definitions, dependencies);
		for (Identifier id : definitions.keySet()) {
			compiler.visit(id); // throws CyclicFactDependencyException
		}

		return new FactDependencyGraph(
				Collections.unmodifiableMap(new LinkedHashMap<>(definitions)),
				Collections.unmodifiableMap(new LinkedHashMap<>(dependencies)),
				List.copyOf(compiler.evaluationOrder),
				Collections.unmodifiableSet(new LinkedHashSet<>(compiler.externalDependencies))
		);
	}

	public Map<Identifier, FactDefinition> definitions() {
		return this.definitions;
	}

	/// Direct fact references for each data-defined fact, including source-fact references.
	public Map<Identifier, Set<Identifier>> dependencies() {
		return this.dependencies;
	}

	/// Data-defined fact ids ordered so every data-defined dependency precedes its consumer.
	public List<Identifier> evaluationOrder() {
		return this.evaluationOrder;
	}

	/// Referenced facts which have no data definition and must be supplied by contributors.
	public Set<Identifier> externalDependencies() {
		return this.externalDependencies;
	}

	/// Resolves required data-defined facts and their transitive dependencies on top of a source state.
	public ContextualState evaluate(ContextualStateAccumulator state, @Nullable Collection<Identifier> requiredFacts) {
		return this.evaluate(state, requiredFacts, null);
	}

	ContextualState evaluate(
			ContextualStateAccumulator state,
			@Nullable Collection<Identifier> requiredFacts,
			@Nullable Identifier debugDomain
	) {
		ContextualState view = state.view();
		for (Identifier id : this.evaluationPlan(requiredFacts)) {
			FactDefinition definition = this.definitions().get(id);
			if (debugDomain != null && ContextualDebug.enabled()) {
				ContextualDebug.PredicateEvaluation evaluation = ContextualDebug.evaluatePredicate(definition.predicate(), view);
				state.contributeFact(id, evaluation.result());
				ContextualDebug.logFact(debugDomain, id, this.dependencies.get(id), evaluation);
			} else {
				state.contributeFact(id, definition.predicate().matches(view));
			}
		}
		return state.snapshot();
	}

	private List<Identifier> evaluationPlan(@Nullable Collection<Identifier> requiredFacts) {
		if (requiredFacts == null) {
			return this.evaluationOrder;
		}
		if (requiredFacts.isEmpty() || this.evaluationOrder.isEmpty()) {
			return List.of();
		}

		return this.evaluationPlans.computeIfAbsent(Set.copyOf(requiredFacts), this::createEvaluationPlan);
	}

	private List<Identifier> createEvaluationPlan(Set<Identifier> requiredFacts) {
		Set<Identifier> requiredClosure = new HashSet<>(requiredFacts);
		for (int index = this.evaluationOrder.size() - 1; index >= 0; index--) {
			Identifier id = this.evaluationOrder.get(index);
			if (requiredClosure.contains(id)) {
				requiredClosure.addAll(this.dependencies.get(id));
			}
		}

		return this.evaluationOrder.stream()
				.filter(requiredClosure::contains)
				.toList();
	}

	private static final class Compiler {
		private final Map<Identifier, FactDefinition> definitions;
		private final Map<Identifier, Set<Identifier>> dependencies;
		private final Map<Identifier, VisitState> visitStates = new HashMap<>();
		private final List<Identifier> path = new ArrayList<>();
		private final List<Identifier> evaluationOrder = new ArrayList<>();
		private final Set<Identifier> externalDependencies = new LinkedHashSet<>();

		private Compiler(
				Map<Identifier, FactDefinition> definitions,
				Map<Identifier, Set<Identifier>> dependencies
		) {
			this.definitions = definitions;
			this.dependencies = dependencies;
		}

		private void visit(Identifier id) throws CyclicFactDependencyException {
			VisitState state = this.visitStates.get(id);
			if (state == VisitState.VISITED) {
				return;
			}
			if (state == VisitState.VISITING) {
				int cycleStart = this.path.indexOf(id);
				List<Identifier> cycle = new ArrayList<>(this.path.subList(cycleStart, this.path.size()));
				cycle.add(id);
				throw new CyclicFactDependencyException(cycle);
			}

			this.visitStates.put(id, VisitState.VISITING);
			this.path.add(id);

			for (Identifier dependency : this.dependencies.get(id)) {
				if (this.definitions.containsKey(dependency)) {
					this.visit(dependency);
				} else {
					this.externalDependencies.add(dependency);
				}
			}

			this.path.removeLast();
			this.visitStates.put(id, VisitState.VISITED);
			this.evaluationOrder.add(id);
		}

		private enum VisitState {
			VISITING,
			VISITED
		}
	}
}
