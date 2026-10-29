/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/// Resolves relative layer placements into a single top-to-bottom order.
/// Layers with no relation between them are ordered by id, so the result
/// does not depend on registration (and therefore mod load) order.
public final class LayerOrdering {
	public record Placement(Set<Identifier> above, Set<Identifier> below, boolean top, boolean bottom) {
		public Placement {
			above = Set.copyOf(above);
			below = Set.copyOf(below);
			if (top && bottom) {
				throw new IllegalArgumentException("A layer cannot be placed at both the top and bottom");
			}
		}

		public boolean isEmpty() {
			return this.above.isEmpty() && this.below.isEmpty() && !this.top && !this.bottom;
		}
	}

	/// @param placements every layer id and its placement
	/// @param warn receives a message for each placement relative to an unregistered layer
	/// @return the layer ids ordered from top to bottom
	/// @throws IllegalStateException if the placements contradict each other
	public static List<Identifier> sort(Map<Identifier, Placement> placements, Consumer<String> warn) {
		// edges point from a context to every context directly below it
		Map<Identifier, Set<Identifier>> below = new HashMap<>();
		Map<Identifier, Integer> aboveCount = new HashMap<>();
		for (Identifier id : placements.keySet()) {
			below.put(id, new TreeSet<>());
			aboveCount.put(id, 0);
		}

		placements.forEach((id, placement) -> {
			for (Identifier other : placement.above()) {
				addEdge(below, id, other, id, warn);
			}
			for (Identifier other : placement.below()) {
				addEdge(below, other, id, id, warn);
			}
			if (placement.top() || placement.bottom()) {
				placements.forEach((otherId, otherPlacement) -> {
					if (placement.top() && !otherPlacement.top()) {
						below.get(id).add(otherId);
					}
					if (placement.bottom() && !otherPlacement.bottom()) {
						below.get(otherId).add(id);
					}
				});
			}
		});
		below.values().forEach(set -> set.forEach(lower -> aboveCount.merge(lower, 1, Integer::sum)));

		TreeSet<Identifier> ready = new TreeSet<>();
		aboveCount.forEach((id, count) -> {
			if (count == 0) {
				ready.add(id);
			}
		});

		List<Identifier> order = new ArrayList<>(placements.size());
		while (!ready.isEmpty()) {
			Identifier next = ready.pollFirst();
			order.add(next);
			for (Identifier lower : below.get(next)) {
				if (aboveCount.merge(lower, -1, Integer::sum) == 0) {
					ready.add(lower);
				}
			}
		}

		if (order.size() != placements.size()) {
			List<Identifier> cyclic = placements.keySet().stream()
					.filter(id -> !order.contains(id))
					.sorted()
					.toList();
			throw new IllegalStateException("Action layer placements form a cycle between: " + cyclic);
		}
		return List.copyOf(order);
	}

	private static void addEdge(
			Map<Identifier, Set<Identifier>> below,
			Identifier upper, Identifier lower,
			Identifier declaredBy, Consumer<String> warn
	) {
		if (!below.containsKey(upper) || !below.containsKey(lower)) {
			Identifier missing = below.containsKey(upper) ? lower : upper;
			warn.accept("Action layer " + declaredBy + " is placed relative to unregistered layer " + missing + "; ignoring");
			return;
		}
		if (upper.equals(lower)) {
			throw new IllegalStateException("Action layer " + declaredBy + " cannot be placed relative to itself");
		}
		below.get(upper).add(lower);
	}

	private LayerOrdering() {
	}
}
