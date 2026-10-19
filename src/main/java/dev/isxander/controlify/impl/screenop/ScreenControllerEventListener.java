/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.impl.controller.ControllerImpl;

public interface ScreenControllerEventListener {
	default void onControllerInput(ControllerImpl controller) {}
}
