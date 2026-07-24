/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.ControllerEntity;

public interface Driver {
	default void addComponents(ControllerEntity controller) {}

	default void update(ControllerEntity controller, boolean outOfFocus) {}

	default void close() {}
}
