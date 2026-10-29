/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LayerOrderingTest {
	private static final Identifier A = id("a");
	private static final Identifier B = id("b");
	private static final Identifier C = id("c");
	private static final Identifier D = id("d");

	@Test
	void relativePlacementsMayReferenceLaterRegistrations() {
		Map<Identifier, LayerOrdering.Placement> placements = new LinkedHashMap<>();
		placements.put(A, below(C));
		placements.put(B, above(A));
		placements.put(C, top());

		assertEquals(List.of(C, B, A), sort(placements));
	}

	@Test
	void topAndBottomBracketEverythingElse() {
		Map<Identifier, LayerOrdering.Placement> placements = new LinkedHashMap<>();
		placements.put(A, bottom());
		placements.put(B, top());
		placements.put(C, above(D));
		placements.put(D, below(C));

		assertEquals(List.of(B, C, D, A), sort(placements));
	}

	@Test
	void unrelatedContextsAreOrderedById() {
		Map<Identifier, LayerOrdering.Placement> placements = new LinkedHashMap<>();
		placements.put(D, top());
		placements.put(C, top());
		placements.put(B, top());

		assertEquals(List.of(B, C, D), sort(placements));
	}

	@Test
	void placementRelativeToUnregisteredContextIsIgnored() {
		Map<Identifier, LayerOrdering.Placement> placements = new LinkedHashMap<>();
		placements.put(A, above(id("missing")));
		placements.put(B, above(A));
		List<String> warnings = new ArrayList<>();

		assertEquals(List.of(B, A), LayerOrdering.sort(placements, warnings::add));
		assertEquals(1, warnings.size());
	}

	@Test
	void contradictoryPlacementsThrow() {
		Map<Identifier, LayerOrdering.Placement> placements = new LinkedHashMap<>();
		placements.put(A, above(B));
		placements.put(B, above(C));
		placements.put(C, above(A));

		assertThrows(IllegalStateException.class, () -> sort(placements));
	}

	private static List<Identifier> sort(Map<Identifier, LayerOrdering.Placement> placements) {
		return LayerOrdering.sort(placements, warning -> {
			throw new AssertionError(warning);
		});
	}

	private static LayerOrdering.Placement above(Identifier other) {
		return new LayerOrdering.Placement(Set.of(other), Set.of(), false, false);
	}

	private static LayerOrdering.Placement below(Identifier other) {
		return new LayerOrdering.Placement(Set.of(), Set.of(other), false, false);
	}

	private static LayerOrdering.Placement top() {
		return new LayerOrdering.Placement(Set.of(), Set.of(), true, false);
	}

	private static LayerOrdering.Placement bottom() {
		return new LayerOrdering.Placement(Set.of(), Set.of(), false, true);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}
}
