/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/// Orders matching registrations by descending priority, then specificity, then
/// newest registration first.
/// Explicit assignability ordering also handles interfaces without relying on a
/// comparator whose ordering would be inconsistent for unrelated interfaces.
final class BehaviourRegistry<T, F> {
	private final List<Registration<T, F>> registrations = new ArrayList<>();
	private boolean locked;

	void register(Class<? extends T> type, int priority, F factory) {
		if (locked) {
			throw new IllegalStateException("Behaviour registration has closed");
		}
		registrations.add(new Registration<>(Objects.requireNonNull(type), priority, Objects.requireNonNull(factory)));
	}

	List<F> factories(T object) {
		List<Registration<T, F>> remaining = new ArrayList<>();
		for (int i = registrations.size() - 1; i >= 0; i--) {
			var registration = registrations.get(i);
			if (registration.type.isInstance(object)) {
				remaining.add(registration);
			}
		}
		// Stable sorting retains newest-first order within a priority.
		remaining.sort(Comparator.<Registration<T, F>>comparingInt(Registration::priority).reversed());
		List<F> ordered = new ArrayList<>(remaining.size());
		while (!remaining.isEmpty()) {
			for (int i = 0; i < remaining.size(); i++) {
				var candidate = remaining.get(i);
				boolean hasMoreSpecific = remaining.stream().anyMatch(other ->
						candidate.priority == other.priority && candidate.type != other.type
						&& candidate.type.isAssignableFrom(other.type));
				if (!hasMoreSpecific) {
					ordered.add(candidate.factory);
					remaining.remove(i);
					break;
				}
			}
		}
		return List.copyOf(ordered);
	}

	void lock() {
		locked = true;
	}

	private record Registration<T, F>(Class<? extends T> type, int priority, F factory) {
	}
}
