package dev.isxander.controlify.controller.gyro;

import dev.isxander.controlify.config.ValueInput;
import dev.isxander.controlify.config.ValueOutput;
import dev.isxander.controlify.controller.ControllerEntity;
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
        public boolean calibrated;
        public boolean delayedCalibration;

        public float lookSensitivity;

        public boolean relativeGyroMode;

        public GyroButtonMode requiresButton;

        public GyroYawMode yawMode;

        public boolean flickStick;

        public boolean invertX;
        public boolean invertY;

        public GyroState calibration;

        @Override
        public void save(ValueOutput output, ControllerEntity controller) {
            output.putBoolean("calibrated", this.calibrated);
            output.putBoolean("delayed_calibration", this.delayedCalibration);
            output.putFloat("look_sensitivity", this.lookSensitivity);
            output.putBoolean("relative_gyro_mode", this.relativeGyroMode);
            output.put("requires_button", GyroButtonMode.CODEC, this.requiresButton);
            output.put("yaw_mode", GyroYawMode.CODEC, this.yawMode);
            output.putBoolean("flick_stick", this.flickStick);
            output.putBoolean("invert_x", this.invertX);
            output.putBoolean("invert_y", this.invertY);

            var obj = output.childObject("calibration");
            obj.putFloat("x", this.calibration.x());
            obj.putFloat("y", this.calibration.y());
            obj.putFloat("z", this.calibration.z());
        }

        @Override
        public void load(ValueInput input, ControllerEntity controller) {
            this.calibrated = input.readBooleanOr("calibrated", false);
            this.delayedCalibration = input.readBooleanOr("delayed_calibration", false);
            this.lookSensitivity = input.readFloatOr("look_sensitivity", 0f);
            this.relativeGyroMode = input.readBooleanOr("relative_gyro_mode", false);
            this.requiresButton = input.readOr("requires_button", GyroButtonMode.CODEC, GyroButtonMode.ON);
            this.yawMode = input.readOr("yaw_mode", GyroYawMode.CODEC, GyroYawMode.YAW);
            this.flickStick = input.readBooleanOr("flick_stick", false);
            this.invertX = input.readBooleanOr("invert_x", false);
            this.invertY = input.readBooleanOr("invert_y", false);

            var obj = input.childObjectOrEmpty("calibration");
            float x = obj.readFloatOr("x", 0f);
            float y = obj.readFloatOr("y", 0f);
            float z = obj.readFloatOr("z", 0f);
            this.calibration = new GyroState(x, y, z);
        }
    }
}
