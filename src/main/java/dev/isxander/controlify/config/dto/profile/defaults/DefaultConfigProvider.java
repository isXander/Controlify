/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.profile.defaults;

import dev.isxander.controlify.config.dto.profile.ProfileConfig;

public interface DefaultConfigProvider {
	boolean isReady();

	ProfileConfig getDefault();
}
