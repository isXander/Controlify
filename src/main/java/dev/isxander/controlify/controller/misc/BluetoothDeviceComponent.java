package dev.isxander.controlify.controller.misc;

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

public class BluetoothDeviceComponent implements ECSComponent, ConfigHolder<BluetoothDeviceComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("bluetooth");

    private final IConfig<Config> config = new ConfigImpl<>(Config::new, Config.class);

    @Override
    public IConfig<Config> config() {
        return config;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static class Config implements ConfigClass {
        public boolean dontShowWarningAgain;

        @Override
        public void load(ValueInput input, ControllerEntity controller) {
            this.dontShowWarningAgain = input.readBooleanOr("dont_show_warning_again", false);
        }

        @Override
        public void save(ValueOutput output, ControllerEntity controller) {
            output.putBoolean("dont_show_warning_again", this.dontShowWarningAgain);
        }
    }
}
