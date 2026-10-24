/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.contextual;

import dev.isxander.controlify.api.controller.Controller;

public interface Context {
	Controller controller();

	GuideVerbosity verbosity();
}
