/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.settings.profile;

import dev.isxander.controlify.impl.config.dto.profile.LightConfig;

public class LightSettings {
	public boolean enabled;

	public LightSettings(boolean enabled) {
		this.enabled = enabled;
	}

	public static LightSettings fromDTO(LightConfig dto) {
		return new LightSettings(dto.enabled());
	}

	public LightConfig toDTO() {
		return new LightConfig(enabled);
	}
}
