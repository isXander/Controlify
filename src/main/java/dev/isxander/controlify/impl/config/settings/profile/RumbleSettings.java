/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.settings.profile;

import dev.isxander.controlify.impl.config.dto.profile.RumbleConfig;
import dev.isxander.controlify.impl.feedback.FeedbackSource;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class RumbleSettings {
	public boolean enabled;
	public Map<Identifier, Float> vibrationStrengths;

	public RumbleSettings(boolean enabled, Map<Identifier, Float> vibrationStrengths) {
		this.enabled = enabled;
		this.vibrationStrengths = new HashMap<>(vibrationStrengths);
	}

	public static RumbleSettings fromDTO(RumbleConfig dto) {
		return new RumbleSettings(dto.enabled(), dto.vibrationStrengths());
	}

	public RumbleConfig toDTO() {
		return new RumbleConfig(enabled, Map.copyOf(vibrationStrengths));
	}


	public float getStrengthForSource(Identifier sourceId) {
		return this.vibrationStrengths.getOrDefault(sourceId, 1.0f);
	}

	/// The factor every output of a playback from this source is multiplied by.
	public float strengthMultiplier(FeedbackSource source) {
		Identifier masterSourceId = FeedbackSource.MASTER.id();
		float masterStrength = this.getStrengthForSource(masterSourceId);
		if (masterSourceId.equals(source.id())) {
			return masterStrength;
		}

		return masterStrength * getStrengthForSource(source.id());
	}
}
