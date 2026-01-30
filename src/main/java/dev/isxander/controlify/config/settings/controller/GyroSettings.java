package dev.isxander.controlify.config.settings.controller;

import dev.isxander.controlify.config.dto.controller.GyroConfig;
import dev.isxander.controlify.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.controller.gyro.GyroStateC;
import dev.isxander.controlify.controller.gyro.GyroYawMode;

public class GyroSettings {
    public CalibrationSettings calibration;
    public float lookSensitivity;
    public boolean relativeMode;
    public boolean invertPitch;
    public boolean invertYaw;
    public GyroButtonMode buttonMode;
    public GyroYawMode yawMode;
    public boolean flickStick;

    public GyroSettings(
            CalibrationSettings calibration,
            float lookSensitivity,
            boolean relativeMode,
            boolean invertPitch,
            boolean invertYaw,
            GyroButtonMode buttonMode,
            GyroYawMode yawMode,
            boolean flickStick
    ) {
        this.calibration = calibration;
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
                CalibrationSettings.fromDTO(dto.calibration()),
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
                calibration.toDTO(),
                lookSensitivity,
                relativeMode,
                invertPitch,
                invertYaw,
                buttonMode,
                yawMode,
                flickStick
        );
    }

    public static class CalibrationSettings {
        public boolean calibrated;
        public boolean delayedCalibration;
        public GyroStateC calibration;

        public CalibrationSettings(boolean calibrated, boolean delayedCalibration, GyroStateC calibration) {
            this.calibrated = calibrated;
            this.delayedCalibration = delayedCalibration;
            this.calibration = calibration;
        }

        public static CalibrationSettings fromDTO(GyroConfig.CalibrationConfig dto) {
            return new CalibrationSettings(dto.calibrated(), dto.delayedCalibration(), dto.calibration());
        }

        public GyroConfig.CalibrationConfig toDTO() {
            return new GyroConfig.CalibrationConfig(calibrated, delayedCalibration, calibration);
        }
    }
}
