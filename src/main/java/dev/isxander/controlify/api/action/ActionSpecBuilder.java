/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.api.controller.Controller;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

public interface ActionSpecBuilder {
	ActionSpecBuilder id(Identifier id);

	ActionSpecBuilder name(Component name);

	ActionSpecBuilder description(Component description);

	ActionSpecBuilder category(Component category);

	/// Accepts IDs from {@link BuiltinActionContexts} or addon IDs registered during {@link ActionEvents#REGISTER_CONTEXTS}.
	ActionSpecBuilder context(Identifier... contexts);

	ActionSpecBuilder shouldExpose(Predicate<Controller> predicate);

	ActionSpec build();
}
