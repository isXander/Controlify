package dev.isxander.controlify.config.settings.profile;

import dev.isxander.controlify.config.dto.profile.DualSenseConfig;

public class DualSenseSettings {
	public boolean triggerEffects;

	public DualSenseSettings(boolean triggerEffects) {
		this.triggerEffects = triggerEffects;
	}

	public static DualSenseSettings fromDTO(DualSenseConfig dto) {
		return new DualSenseSettings(dto.triggerEffects());
	}

	public DualSenseConfig toDTO() {
		return new DualSenseConfig(triggerEffects);
	}
}
