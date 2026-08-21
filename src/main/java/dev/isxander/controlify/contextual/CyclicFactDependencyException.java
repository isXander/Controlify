/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public class CyclicFactDependencyException extends IllegalArgumentException {
	private final List<Identifier> cycle;

	public CyclicFactDependencyException(List<Identifier> cycle) {
		super("Cyclic contextual fact dependency: " + cycle.stream()
				.map(Identifier::toString)
				.collect(Collectors.joining(" -> ")));
		this.cycle = List.copyOf(cycle);
	}

	/// The cycle in traversal order, with the first identifier repeated at the end.
	public List<Identifier> cycle() {
		return this.cycle;
	}
}
