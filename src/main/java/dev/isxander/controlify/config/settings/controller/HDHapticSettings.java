package dev.isxander.controlify.config.settings.controller;

import dev.isxander.controlify.config.dto.controller.HDHapticConfig;

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
