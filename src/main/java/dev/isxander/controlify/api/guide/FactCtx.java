/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.controller.ControllerEntity;

public interface FactCtx {
	ControllerEntity controller();

	GuideVerbosity verbosity();
}
