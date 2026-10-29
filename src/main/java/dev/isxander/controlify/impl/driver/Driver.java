/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver;

import dev.isxander.controlify.impl.controller.ControllerImpl;

public interface Driver {
	default void addComponents(ControllerImpl controller) {}

	default void update(ControllerImpl controller) {}

	/// Enables resources needed only by the active controller, such as sensor sampling.
	/// Buttons and axes must remain available while inactive for controller selection.
	default void setActive(boolean active) {}

	default void close() {}
}
