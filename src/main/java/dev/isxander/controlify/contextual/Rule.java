/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

public interface Rule<K> {
	/// Used to identify require multiple rules are overriding each other.
	///
	/// For example, with guides this would be the binding and location.
	///
	/// Allows deduplication and short-circuiting done by the rule engine.
	K key();

	ContextualPredicate predicate();

	default boolean matches(ContextualState state) {
		return this.predicate().matches(state);
	}
}
