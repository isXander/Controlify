package dev.isxander.controlify.controller.gyro;

import dev.isxander.controlify.config.settings.controller.GyroSettings;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class GyroComponent extends ECSComponentImpl {
    public static final Identifier ID = CUtil.rl("gyro");

    private GyroStateC gyroState = GyroStateC.ZERO;

    public GyroStateC getState() {
        return this.gyroState;
    }

    public void setState(GyroStateC state) {
        this.gyroState = state;
    }

    public GyroSettings settings() {
        return this.controller().settings().gyro;
    }

    public GyroSettings defaultSettings() {
        return this.controller().defaultSettings().gyro;
    }

    @Override
    public Identifier id() {
        return ID;
    }

}
