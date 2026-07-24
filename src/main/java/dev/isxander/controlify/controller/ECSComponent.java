/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller;

import net.minecraft.resources.Identifier;

public interface ECSComponent {
	Identifier id();

	default void attach(ControllerEntity controller) {

	}
}
