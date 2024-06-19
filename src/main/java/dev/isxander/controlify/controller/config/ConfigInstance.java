package dev.isxander.controlify.controller.config;

public interface ConfigInstance<T extends ConfigObject> {
    T getConfig();

    void applyConfig(T config);

    T getDefaultConfig();

    ConfigModule<T> module();

}
