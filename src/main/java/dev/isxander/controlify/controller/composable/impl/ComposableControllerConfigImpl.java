package dev.isxander.controlify.controller.composable.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerConfig;
import dev.isxander.controlify.controller.composable.ComposableController;
import dev.isxander.controlify.controller.composable.ComposableControllerConfig;
import dev.isxander.controlify.utils.CUtil;
import org.apache.commons.lang3.SerializationUtils;

public class ComposableControllerConfigImpl<T extends ControllerConfig> implements ComposableControllerConfig<T> {
    private T config;
    private final T defaultConfig;

    public ComposableControllerConfigImpl(T defaultConfig) {
        this.defaultConfig = defaultConfig;
        this.config = (T) defaultConfig.clone();
    }

    @Override
    public T config() {
        return config;
    }

    @Override
    public T defaultConfig() {
        return defaultConfig;
    }

    @Override
    public void resetConfig() {
        this.config = SerializationUtils.clone(defaultConfig());
    }

    @Override
    public void setConfig(Gson gson, JsonElement json, ComposableController<?> controller) {
        T newConfig;
        try {
            newConfig = gson.fromJson(json, new TypeToken<T>(getClass()){}.getType());
        } catch (Throwable e) {
            CUtil.LOGGER.error("Could not set config for controller {} ({})! Using default config instead. Printing json: {}", controller.name(), controller.uid(), json.toString(), e);
            Controlify.instance().config().setDirty();
            return;
        }

        if (newConfig != null) {
            this.config = newConfig;
        } else {
            CUtil.LOGGER.error("Could not set config for controller {} ({})! Using default config instead.", controller.name(), controller.uid());
            this.config = SerializationUtils.clone(defaultConfig());
            Controlify.instance().config().setDirty();
        }

        this.config.validateRadialActions(controller.bindings());
    }
}
