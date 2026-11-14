/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.ActionLayerMode;
import dev.isxander.controlify.api.action.Gesture;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.activator.ActivatorConfig;
import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/// Finds bindings where one press of an input would activate more than intended.
/// Purely static: it reasons about gestures, inputs and the context stack, not live input.
public final class BindingConflicts {
	public enum Kind {
		/// Both bindings fire on the same press, in a context they share.
		CONFLICT,
		/// The binding's button is a modifier of another action's chord, and the binding
		/// fires before the chord can claim it.
		MODIFIER_LEAK,
		/// Another action in a higher overlay context takes this input from this binding while that context is active.
		/// A context that is hidden outright, by a blocking context, is not shadowed: none of its bindings compete.
		SHADOWED,
		/// The same action already has this input with the same gestures.
		REDUNDANT,
	}

	/// @param bindings the bindings to check, which may be unsaved
	public record Entry(Identifier action, Set<Identifier> contexts, List<ActionBinding> bindings, ActivatorConfig<?> activator) {
	}

	/// Reported on the binding at {@code bindingIndex} of {@code action}, caused by {@code other}'s binding.
	public record Conflict(Kind kind, Identifier action, int bindingIndex, Identifier other, int otherBindingIndex) {
	}

	/// @param layerOrder every layer, top to bottom
	public static List<Conflict> find(List<Entry> entries, List<ActionLayer> layerOrder) {
		Map<Identifier, Integer> position = new HashMap<>();
		Map<Identifier, ActionLayer> layers = new HashMap<>();
		for (int i = 0; i < layerOrder.size(); i++) {
			position.put(layerOrder.get(i).id(), i);
			layers.put(layerOrder.get(i).id(), layerOrder.get(i));
		}
		Map<Identifier, Integer> reach = new HashMap<>();
		for (ActionLayer layer : layerOrder) {
			reach.put(layer.id(), reach(layer, position, layers));
		}

		List<Conflict> conflicts = new ArrayList<>();
		for (Entry a : entries) {
			for (int i = 0; i < a.bindings().size(); i++) {
				ActionBinding bindingA = a.bindings().get(i);
				List<Optional<Gesture>> gesturesA = gestures(a.activator(), bindingA);

				for (int j = 0; j < i; j++) {
					ActionBinding earlier = a.bindings().get(j);
					if (earlier.input().equals(bindingA.input()) && gestures(a.activator(), earlier).equals(gesturesA)) {
						conflicts.add(new Conflict(Kind.REDUNDANT, a.action(), i, a.action(), j));
					}
				}

				for (Entry b : entries) {
					if (b == a) {
						continue;
					}
					boolean shareContext = !Collections.disjoint(a.contexts(), b.contexts());
					for (int j = 0; j < b.bindings().size(); j++) {
						ActionBinding bindingB = b.bindings().get(j);

						if (bindingA.input().equals(bindingB.input())) {
							if (!overlap(gesturesA, gestures(b.activator(), bindingB))) {
								continue;
							}
							if (shareContext) {
								conflicts.add(new Conflict(Kind.CONFLICT, a.action(), i, b.action(), j));
							} else if (shadows(b.contexts(), a.contexts(), position, layers, reach)) {
								conflicts.add(new Conflict(Kind.SHADOWED, a.action(), i, b.action(), j));
							}
						} else if (shareContext
								&& bindingA.input() instanceof LogicalInput.Single(Identifier button)
								&& bindingB.input() instanceof LogicalInput.ModifiedChord chord
								&& chord.modifiers().contains(button)
								&& gesturesA.stream().anyMatch(BindingConflicts::firesBeforeRelease)) {
							conflicts.add(new Conflict(Kind.MODIFIER_LEAK, a.action(), i, b.action(), j));
						}
					}
				}
			}
		}
		return conflicts;
	}

	/// The gesture of every leaf for this binding; empty for continuous leaves.
	private static List<Optional<Gesture>> gestures(ActivatorConfig<?> activator, ActionBinding binding) {
		return activator.leaves().stream().map(leaf -> leaf.gestureFor(binding)).toList();
	}

	private static boolean overlap(List<Optional<Gesture>> a, List<Optional<Gesture>> b) {
		for (Optional<Gesture> gestureA : a) {
			for (Optional<Gesture> gestureB : b) {
				if (!Collections.disjoint(outcomes(gestureA), outcomes(gestureB))) {
					return true;
				}
			}
		}
		return false;
	}

	/// Continuous leaves respond to any movement, so they occupy every outcome.
	private static Set<Gesture.Outcome> outcomes(Optional<Gesture> gesture) {
		return gesture.map(Gesture::outcomes).orElseGet(() -> EnumSet.allOf(Gesture.Outcome.class));
	}

	private static boolean firesBeforeRelease(Optional<Gesture> gesture) {
		return gesture.map(Gesture::firesBeforeRelease).orElse(true);
	}

	/// Whether some upper context consumes inputs that some lower context would otherwise receive.
	private static boolean shadows(
			Set<Identifier> upper, Set<Identifier> lower,
			Map<Identifier, Integer> position, Map<Identifier, ActionLayer> layers, Map<Identifier, Integer> reach
	) {
		for (Identifier upperContext : upper) {
			ActionLayer upperLayer = layers.get(upperContext);
			if (upperLayer == null || upperLayer.mode() == ActionLayerMode.TRANSPARENT) {
				continue;
			}
			int upperPosition = position.get(upperContext);
			int upperReach = reach.get(upperContext);
			for (Identifier lowerContext : lower) {
				Integer lowerPosition = position.get(lowerContext);
				if (lowerPosition != null && upperPosition < lowerPosition && lowerPosition <= upperReach) {
					return true;
				}
			}
		}
		return false;
	}

	/// The position of the lowest layer that can receive input while this layer is active: the nearest
	/// blocking layer at or below it that is always active alongside it, being itself or one it requires.
	private static int reach(ActionLayer layer, Map<Identifier, Integer> position, Map<Identifier, ActionLayer> layers) {
		int layerPosition = position.get(layer.id());
		int reach = Integer.MAX_VALUE;
		Set<Identifier> seen = new HashSet<>(Set.of(layer.id()));
		Deque<ActionLayer> pending = new ArrayDeque<>(List.of(layer));
		while (!pending.isEmpty()) {
			ActionLayer alwaysActive = pending.pop();
			int alwaysActivePosition = position.get(alwaysActive.id());
			if (alwaysActive.mode() == ActionLayerMode.BLOCKING && alwaysActivePosition >= layerPosition) {
				reach = Math.min(reach, alwaysActivePosition);
			}
			for (Identifier requiredId : alwaysActive.requires()) {
				ActionLayer required = layers.get(requiredId);
				if (required != null && seen.add(requiredId)) {
					pending.push(required);
				}
			}
		}
		return reach;
	}

	private BindingConflicts() {
	}
}
