/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.controller.input.InputComponent;
import dev.isxander.controlify.impl.action.ActionAccessorImpl;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// Resolves the current action on a controller or input component. Retain this accessor;
/// profile changes may replace resolved action instances.
public interface ActionAccessor {
	Codec<ActionAccessor> CODEC = Identifier.CODEC.xmap(
			ActionAccessorImpl::new, ActionAccessor::actionId);

	@Nullable Action onOrNull(InputComponent inputComponent);

	Identifier actionId();

	default @Nullable Action onOrNull(Controller controller) {
		return onOrNull(controller.input());
	}

	default Action on(InputComponent inputComponent) {
		Action action = onOrNull(inputComponent);
		if (action == null) {
			throw new NullPointerException("No action bound to input component " + actionId());
		}
		return action;
	}

	default Action on(Controller controller) {
		return on(controller.input());
	}
}
