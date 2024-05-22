package dev.isxander.controlify.controller.misc;

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

    public static class Config implements ConfigClass {
        public boolean dontShowWarningAgain = false;
    }
}
