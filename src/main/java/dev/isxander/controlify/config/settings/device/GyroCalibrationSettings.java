package dev.isxander.controlify.config.settings.device;

import dev.isxander.controlify.config.dto.device.GyroCalibrationConfig;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.gyro.GyroStateC;

public class GyroCalibrationSettings {
    public GyroStateC offset;

    private static final GyroCalibrationSettings DEFAULT = new GyroCalibrationSettings();

    private GyroCalibrationSettings() {
        this.offset = new GyroState();
    }

    public GyroCalibrationSettings(
            GyroStateC offset
    ) {
        this.offset = offset;
    }

    public static GyroCalibrationSettings defaults() {
        return DEFAULT;
    }

    public static GyroCalibrationSettings fromDTO(GyroCalibrationConfig dto) {
        return new GyroCalibrationSettings(
                dto.offset()
        );
    }

    public GyroCalibrationConfig toDTO() {
        return new GyroCalibrationConfig(
                offset
        );
    }
}
