package dev.isxander.controlify.controller.gyro;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.config.*;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public class GyroComponent implements ComponentWithConfig<GyroComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("gyro");
    public static final ConfigModule<Config> CONFIG_MODULE = new ConfigModule<>(ID, Config.class);

    private GyroStateC gyroState = GyroStateC.ZERO;
    private final ConfigInstance<Config> config;

    public GyroComponent(ControllerEntity controller) {
        this.config = new ConfigInstanceImpl<>(ID, ModuleRegistry.INSTANCE, controller);
    }

    public GyroStateC getState() {
        return this.gyroState;
    }

    public void setState(GyroStateC state) {
        this.gyroState = state;
    }

    @Override
    public ConfigInstance<Config> getConfigInstance() {
        return config;
    }

    public static class Config implements ConfigObject {
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
