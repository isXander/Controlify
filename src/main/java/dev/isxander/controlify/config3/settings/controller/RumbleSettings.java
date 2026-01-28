package dev.isxander.controlify.config3.settings.controller;

import dev.isxander.controlify.config3.dto.controller.RumbleConfig;
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
}
