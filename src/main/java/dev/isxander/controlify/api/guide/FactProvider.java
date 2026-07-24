/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.guide;

@FunctionalInterface
public interface FactProvider<T> {
	boolean test(T t);

	static <Z> FactProvider<Z> staticProvider(boolean value) {
		return t -> value;
	}
}
