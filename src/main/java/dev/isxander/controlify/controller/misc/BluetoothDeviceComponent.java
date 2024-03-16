package dev.isxander.controlify.controller.misc;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ConfigClass;
import dev.isxander.controlify.controller.ConfigHolder;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.IConfig;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import net.minecraft.resources.ResourceLocation;

public class BluetoothDeviceComponent implements ECSComponent, ConfigHolder<BluetoothDeviceComponent.Config> {
    public static final ResourceLocation ID = Controlify.id("bluetooth");

    private final IConfig<Config> config = new ConfigImpl<>(Config::new, Config.class);

    @Override
    public IConfig<Config> config() {
        return config;
    }

    public static class Config implements ConfigClass {
        public boolean dontShowWarningAgain = false;
    }
}
