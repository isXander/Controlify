package dev.isxander.controlify.config3.dto.controller.defaults;

import dev.isxander.controlify.config3.dto.controller.ControllerConfig;
import net.minecraft.resources.Identifier;

public interface DefaultConfigProvider {
    ControllerConfig getDefaultForNamespace(Identifier namespace);
}
