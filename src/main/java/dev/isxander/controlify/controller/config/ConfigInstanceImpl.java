package dev.isxander.controlify.controller.config;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.resources.ResourceLocation;

public class ConfigInstanceImpl<T extends ConfigObject> implements ConfigInstance<T> {
    private final ConfigModule<T> module;
    private final ControllerEntity controller;
    private T config;
    private final T defaultConfig;

    public ConfigInstanceImpl(ResourceLocation id, ModuleRegistry registry, ControllerEntity controller) {
        this.module = registry.getModule(id);
        this.config = registry.getDefaultConfig(id, controller);
        this.defaultConfig = registry.getDefaultConfig(id, controller);
        this.controller = controller;
    }

    @Override
    public T getConfig() {
        return this.config;
    }

    @Override
    public void applyConfig(T config) {
        this.config = config;
        this.config.onApply(controller);
    }

    @Override
    public T getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ConfigModule<T> module() {
        return module;
    }
}
