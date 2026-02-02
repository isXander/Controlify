package dev.isxander.controlify.config.settings.profile;

import dev.isxander.controlify.config.dto.device.GyroCalibrationConfig;
import dev.isxander.controlify.config.dto.profile.GyroConfig;
import dev.isxander.controlify.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.controller.gyro.GyroStateC;
import dev.isxander.controlify.controller.gyro.GyroYawMode;

public class GyroSettings {
    public float lookSensitivity;
    public boolean relativeMode;
    public boolean invertPitch;
    public boolean invertYaw;
    public GyroButtonMode buttonMode;
    public GyroYawMode yawMode;
    public boolean flickStick;

    public GyroSettings(
            float lookSensitivity,
            boolean relativeMode,
            boolean invertPitch,
            boolean invertYaw,
            GyroButtonMode buttonMode,
            GyroYawMode yawMode,
            boolean flickStick
    ) {
        this.lookSensitivity = lookSensitivity;
        this.relativeMode = relativeMode;
        this.invertPitch = invertPitch;
        this.invertYaw = invertYaw;
        this.buttonMode = buttonMode;
        this.yawMode = yawMode;
        this.flickStick = flickStick;
    }

    public static GyroSettings fromDTO(GyroConfig dto) {
        return new GyroSettings(
                dto.lookSensitivity(),
                dto.relativeMode(),
                dto.invertPitch(),
                dto.invertYaw(),
                dto.buttonMode(),
                dto.yawMode(),
                dto.flickStick()
        );
    }

    public GyroConfig toDTO() {
        return new GyroConfig(
                lookSensitivity,
                relativeMode,
                invertPitch,
                invertYaw,
                buttonMode,
                yawMode,
                flickStick
        );
    }
}
