package dev.isxander.controlify.controller.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Stream;

public class ModuleRegistryImpl implements ModuleRegistry {
    private final Map<ResourceLocation, ConfigModule<?>> modules;
    private final List<DefaultSource> defaultSources;

    public ModuleRegistryImpl() {
        this.modules = new HashMap<>();
        this.defaultSources = new ArrayList<>();
    }

    @Override
    public <T extends ConfigObject> T getDefaultConfig(ResourceLocation id, ControllerEntity controller) {
        Stream<JsonObject> defaultStack = Lists.reverse(defaultSources)
                .stream()
                .map(source -> source.createDefaultConfig(id));
        // merge all defaults into one
        JsonObject singleObject = defaultStack.reduce(new JsonObject(), (a, b) -> {
            b.entrySet().forEach(entry -> a.add(entry.getKey(), entry.getValue()));
            return a;
        });

        ConfigModule<T> module = getModule(id);

        T defaultObject = module.deserialize(singleObject);
        defaultObject.applyControllerSpecificDefaults(controller);

        return defaultObject;
    }

    @Override
    public Collection<ConfigModule<?>> getModules() {
        return modules.values();
    }

    @Override
    public void registerModule(ConfigModule<?> module) {
        this.modules.put(module.id(), module);
    }

    @Override
    public <T extends ConfigObject> ConfigModule<T> getModule(ResourceLocation id) {
        return (ConfigModule<T>) this.modules.get(id);
    }

    @Override
    public void registerDefaultSource(DefaultSource defaultSource) {
        this.defaultSources.add(defaultSource);
    }
}
