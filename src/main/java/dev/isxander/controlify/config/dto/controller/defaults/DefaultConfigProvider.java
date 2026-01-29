package dev.isxander.controlify.config.dto.controller.defaults;

import dev.isxander.controlify.config.dto.controller.ControllerConfig;
import dev.isxander.controlify.controller.id.ControllerType;
import net.minecraft.resources.Identifier;

public interface DefaultConfigProvider {
    boolean isReady();

    ControllerConfig getDefaultForNamespace(Identifier namespace);

    default ControllerConfig getDefault() {
        return getDefaultForNamespace(ControllerType.DEFAULT.namespace());
    }
}
