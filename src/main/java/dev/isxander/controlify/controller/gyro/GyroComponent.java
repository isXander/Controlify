package dev.isxander.controlify.controller.gyro;

import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public class GyroComponent implements ECSComponent, ConfigHolder<GyroComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("gyro");

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

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static class Config implements ConfigClass {
        public boolean calibrated = false;
        public boolean delayedCalibration = false;

        public float lookSensitivity = 0f;

        public boolean relativeGyroMode = false;

        public GyroButtonMode requiresButton = GyroButtonMode.ON;

        public GyroYawMode yawMode = GyroYawMode.YAW;

        public boolean flickStick = false;

        public boolean invertX = false;
        public boolean invertY = false;

        public GyroState calibration = new GyroState();
    }
}
