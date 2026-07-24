/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.entrypoint;

import dev.isxander.controlify.api.ControlifyApi;

public interface InitContext {

	ControlifyApi controlify();
}
