/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.settings.profile;

import dev.isxander.controlify.impl.config.dto.profile.RumbleConfig;
import dev.isxander.controlify.impl.feedback.FeedbackGroup;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
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


	public float getStrengthForGroup(Identifier groupId) {
		return this.vibrationStrengths.getOrDefault(groupId, 1.0f);
	}

	/// The factor every output of a playback in these groups is multiplied by: the master
	/// strength times the weakest of the groups, so turning any one of them down affects the
	/// effect. [FeedbackGroup#UNGROUPED] has no strength of its own.
	public float strengthMultiplier(List<FeedbackGroup> groups) {
		float weakest = 1f;
		boolean first = true;
		for (FeedbackGroup group : groups) {
			if (!group.equals(FeedbackGroup.UNGROUPED)) {
				float strength = this.getStrengthForGroup(group.id());
				weakest = first ? strength : Math.min(weakest, strength);
				first = false;
			}
		}
		return this.getStrengthForGroup(FeedbackGroup.MASTER_STRENGTH) * weakest;
	}
}
