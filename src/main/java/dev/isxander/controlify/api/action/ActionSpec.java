/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.impl.action.ActionSpecBuilderImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

/// Immutable information about an {@link Action}.
/// @param id the unique identifier for the action
/// @param name the display name for the action
/// @param description the display description for the action
/// @param category the category for the action
/// @param contexts the set of context IDs that this action should work in
/// @param shouldExpose predicate deciding if the action should be visible in the settings and available
///                     to bind for the current controller
public record ActionSpec(
		Identifier id,
		Component name,
		Component description,
		Component category,
		Set<Identifier> contexts,
		Predicate<Controller> shouldExpose
) implements Comparable<ActionSpec> {
	public ActionSpec {
		contexts = Set.copyOf(contexts);
	}

	@Override
	public int compareTo(ActionSpec o) {
		return Comparator.comparing(ActionSpec::id)
				.compare(this, o);
	}

	public static ActionSpecBuilder builder() {
		return new ActionSpecBuilderImpl();
	}
}
