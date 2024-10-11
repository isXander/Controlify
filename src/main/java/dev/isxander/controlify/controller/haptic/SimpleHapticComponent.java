package dev.isxander.controlify.controller.haptic;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public class SimpleHapticComponent implements ECSComponent, ConfigHolder<SimpleHapticComponent.Config> {
    public static final ResourceLocation ID = CUtil.rl("hd_haptics");

    private final IConfig<Config> config = new ConfigImpl<>(Config::new, Config.class);
    private Runnable onHaptic;

    public void playHaptic() {
        if (confObj().enabled) {
            onHaptic.run();
        }
    }

    public void applyOnHaptic(Runnable onHaptic) {
        this.onHaptic = onHaptic;
    }

    @Override
    public IConfig<Config> config() {
        return config;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static class Config implements ConfigClass {
        public boolean enabled;
    }
}
