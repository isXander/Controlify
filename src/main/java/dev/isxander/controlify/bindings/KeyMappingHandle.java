/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings;

import dev.isxander.controlify.controller.ControllerEntity;

import java.util.function.BooleanSupplier;

public interface KeyMappingHandle {
	void controlify$setPressed(boolean isDown);

	void controlify$addToggleCondition(ControllerEntity controller, BooleanSupplier condition);
}
