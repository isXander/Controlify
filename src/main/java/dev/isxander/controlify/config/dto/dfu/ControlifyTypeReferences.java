/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DSL;

public final class ControlifyTypeReferences {

	/** Legacy monolithic client configuration, retained for pre-v3 migration. */
	public static final DSL.TypeReference USER_STATE = () -> "controlify:user_state";
	/** Shared global and device configuration introduced in v3. */
	public static final DSL.TypeReference SHARED_CONFIG = () -> "controlify:shared_config";
	/** Per-profile configuration introduced in v3. */
	public static final DSL.TypeReference PROFILE_CONFIG = () -> "controlify:profile_config";

	private ControlifyTypeReferences() {
	}
}
