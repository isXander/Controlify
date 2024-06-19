package dev.isxander.controlify.controller.config;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface ModuleRegistry {
    ModuleRegistry INSTANCE = new ModuleRegistryImpl();

    <T extends ConfigObject> T getDefaultConfig(ResourceLocation id, ControllerEntity controller);

    Collection<ConfigModule<?>> getModules();

    void registerModule(ConfigModule<?> module);

    <T extends ConfigObject> ConfigModule<T> getModule(ResourceLocation id);

    void registerDefaultSource(DefaultSource defaultSource);

}
