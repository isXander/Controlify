/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.settings.profile;

import dev.isxander.controlify.config.dto.profile.DualsenseConfig;

public class DualsenseSettings {
	public boolean triggerEffects;

	public DualsenseSettings(boolean triggerEffects) {
		this.triggerEffects = triggerEffects;
	}

	public static DualsenseSettings fromDTO(DualsenseConfig dto) {
		return new DualsenseSettings(dto.triggerEffects());
	}

	public DualsenseConfig toDTO() {
		return new DualsenseConfig(triggerEffects);
	}
}
