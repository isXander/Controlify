/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.ingame.look;

import dev.isxander.controlify.api.controller.Controller;

/// Creates the [LookSource] of one controller.
@FunctionalInterface
public interface LookSourceFactory {
	LookSource create(Controller controller);
}
