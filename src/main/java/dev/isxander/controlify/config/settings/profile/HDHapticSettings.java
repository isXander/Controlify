/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.settings.profile;

import dev.isxander.controlify.config.dto.profile.HDHapticConfig;

public class HDHapticSettings {
	public boolean enabled;

	public HDHapticSettings(boolean enabled) {
		this.enabled = enabled;
	}

	public static HDHapticSettings fromDTO(HDHapticConfig dto) {
		return new HDHapticSettings(dto.enabled());
	}

	public HDHapticConfig toDTO() {
		return new HDHapticConfig(enabled);
	}
}
