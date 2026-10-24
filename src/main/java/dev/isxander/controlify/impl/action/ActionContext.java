/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

/// Internal context definition; the public API exposes its identifier and registration parameters only.
public record ActionContext(Identifier id, Function<Minecraft, Boolean> isApplicable) implements Comparable<ActionContext> {
	@Override
	public int compareTo(@NonNull ActionContext o) {
		return this.id().compareTo(o.id());
	}
}
