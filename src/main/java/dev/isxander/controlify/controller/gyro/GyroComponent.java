package dev.isxander.controlify.controller.gyro;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ConfigClass;
import dev.isxander.controlify.controller.ConfigHolder;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.IConfig;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import net.minecraft.resources.ResourceLocation;

public class GyroComponent implements ECSComponent, ConfigHolder<GyroComponent.Config> {
    public static final ResourceLocation ID = Controlify.id("gyro");

    private GyroStateC gyroState = GyroStateC.ZERO;
    private final IConfig<Config> config = new ConfigImpl<>(Config::new, Config.class);

    public GyroStateC getState() {
        return this.gyroState;
    }

    public void setState(GyroStateC state) {
        this.gyroState = state;
    }

    @Override
    public IConfig<Config> config() {
        return this.config;
    }

    public static class Config implements ConfigClass {
        public boolean calibrated = false;
        public boolean delayedCalibration = false;

        public float lookSensitivity = 0f;

        public boolean relativeGyroMode = false;

        public boolean requiresButton = true;

        public GyroYawMode yawMode = GyroYawMode.YAW;

        public boolean flickStick = false;

        public boolean invertX = false;
        public boolean invertY = false;

        public GyroState calibration = new GyroState();
    }
}
