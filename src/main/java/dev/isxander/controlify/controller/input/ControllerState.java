/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.controller.impl.ControllerStateImpl;

public interface ControllerState extends ControllerStateView {
	ControllerState EMPTY = new ControllerStateImpl();

	void clearState();

	default void close() {}
}
