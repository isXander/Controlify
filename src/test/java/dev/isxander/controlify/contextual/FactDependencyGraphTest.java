/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactDependencyGraphTest {
	private static final Identifier ROOT = id("root");
	private static final Identifier MIDDLE = id("middle");
	private static final Identifier LEAF = id("leaf");
	private static final Identifier UNUSED = id("unused");
	private static final Identifier SOURCE = id("source");
	private static final Identifier FROM_SOURCE = id("from_source");

	@Test
	void evaluatesTransitiveDependenciesBeforeRequiredFact() {
		FactDependencyGraph graph = FactDependencyGraph.compile(List.of(
				new FactDefinition(LEAF, new ContextualPredicate.Fact(MIDDLE)),
				new FactDefinition(MIDDLE, new ContextualPredicate.Fact(ROOT)),
				new FactDefinition(ROOT, new ContextualPredicate.Static(true)),
				new FactDefinition(UNUSED, new ContextualPredicate.Static(true))
		));

		ContextualState state = graph.evaluate(new ContextualStateAccumulator(), Set.of(LEAF));

		assertTrue(state.facts().get(ROOT));
		assertTrue(state.facts().get(MIDDLE));
		assertTrue(state.facts().get(LEAF));
		assertFalse(state.facts().containsKey(UNUSED));
	}

	@Test
	void resolvesExternalDependenciesFromSourceState() {
		FactDependencyGraph graph = FactDependencyGraph.compile(List.of(
				new FactDefinition(FROM_SOURCE, new ContextualPredicate.Fact(SOURCE))
		));
		ContextualStateAccumulator accumulator = new ContextualStateAccumulator();
		accumulator.contributeFact(SOURCE, true);

		ContextualState state = graph.evaluate(accumulator, Set.of(FROM_SOURCE));

		assertEquals(Set.of(SOURCE, FROM_SOURCE), state.facts().keySet());
		assertTrue(state.facts().get(FROM_SOURCE));
	}

	@Test
	void evaluatesNoDefinitionsWhenNoneAreRequired() {
		FactDependencyGraph graph = FactDependencyGraph.compile(List.of(
				new FactDefinition(ROOT, new ContextualPredicate.Static(true))
		));

		ContextualState state = graph.evaluate(new ContextualStateAccumulator(), Set.of());

		assertTrue(state.facts().isEmpty());
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}
}
