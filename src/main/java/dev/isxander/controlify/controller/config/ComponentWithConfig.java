package dev.isxander.controlify.controller.config;

import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.ECSComponent;

public interface ComponentWithConfig<T extends ConfigObject> extends ECSComponent {
    ConfigInstance<T> getConfigInstance();

    default JsonObject toJson() {
        ConfigModule<T> module = getConfigInstance().module();
        return module.serialize(getConfigInstance().getConfig(), getConfigInstance().getDefaultConfig());
    }

    default void fromJson(JsonObject json) {
        T config = getConfigInstance().module().deserialize(json);
        getConfigInstance().applyConfig(config);
    }

    default T confObj() {
        return getConfigInstance().getConfig();
    }

    default T defObj() {
        return getConfigInstance().getDefaultConfig();
    }
}
