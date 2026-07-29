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
