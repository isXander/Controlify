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
import org.jetbrains.annotations.Contract;

import java.util.function.Predicate;

public interface ActionSpecBuilder {
	@Contract("_ -> this")
	ActionSpecBuilder id(Identifier id);

	@Contract("_ -> this")
	ActionSpecBuilder name(Component name);

	@Contract("_ -> this")
	ActionSpecBuilder description(Component description);

	@Contract("_ -> this")
	ActionSpecBuilder category(Component category);

	/// Accepts IDs from {@link BuiltinActionLayers} or addon IDs registered during {@link ActionEvents#REGISTER_LAYERS}.
	/// An action in several layers receives input through whichever of them is reachable.
	@Contract("_ -> this")
	ActionSpecBuilder layer(Identifier... contexts);

	@Contract("_ -> this")
	ActionSpecBuilder shouldExpose(Predicate<Controller> predicate);

	ActionSpec build();
}
