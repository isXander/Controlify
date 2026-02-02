package dev.isxander.controlify.config.dto.profile.defaults;

import dev.isxander.controlify.config.dto.profile.ProfileConfig;
import dev.isxander.controlify.controller.id.ControllerType;
import net.minecraft.resources.Identifier;

public interface DefaultConfigProvider {
    boolean isReady();

    ProfileConfig getDefaultForNamespace(Identifier namespace);

    default ProfileConfig getDefault() {
        return getDefaultForNamespace(ControllerType.DEFAULT.namespace());
    }
}
