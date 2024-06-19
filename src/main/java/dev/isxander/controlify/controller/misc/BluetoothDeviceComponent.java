package dev.isxander.controlify.controller.misc;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.config.*;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public class BluetoothDeviceComponent implements ComponentWithConfig<BluetoothDeviceComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("bluetooth");
    public static final ConfigModule<Config> CONFIG_MODULE = new ConfigModule<>(ID, Config.class);

    private final ConfigInstance<Config> config;

    public BluetoothDeviceComponent(ControllerEntity controller) {
        this.config = new ConfigInstanceImpl<>(ID, ModuleRegistry.INSTANCE, controller);
    }

    @Override
    public ConfigInstance<Config> getConfigInstance() {
        return config;
    }

    public static class Config implements ConfigObject {
        public boolean dontShowWarningAgain = false;
    }
}
