package dev.isxander.controlify.config.settings.controller;

import dev.isxander.controlify.config.dto.controller.RumbleConfig;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
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

    public RumbleState applyRumbleStrength(RumbleState baseStrength, RumbleSource source) {
        Identifier masterSourceId = RumbleSource.MASTER.id();
        float masterStrength = this.getStrengthForSource(masterSourceId);
        if (masterSourceId.equals(source.id())) {
            return baseStrength.mul(masterStrength);
        }

        float strengthMultiplier = masterStrength * getStrengthForSource(source.id());
        return baseStrength.mul(strengthMultiplier);
    }
}
