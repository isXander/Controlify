/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DSL;

public final class ControlifyTypeReferences {

	public static final DSL.TypeReference USER_STATE = () -> "controlify:user_state";

	private ControlifyTypeReferences() {
	}
}
