package dev.isxander.controlify.config3.settings;

import dev.isxander.controlify.config3.dto.ControlifyConfig;
import dev.isxander.controlify.config3.settings.controller.ControllerSettings;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ControlifySettings {
    public @Nullable String currentControllerUID;
    public ControllerSettings controllerSettings;
    public GlobalSettings globalSettings;

    public ControlifySettings(
            @Nullable String currentControllerUID,
            ControllerSettings controllerSettings,
            GlobalSettings globalSettings
    ) {
        this.currentControllerUID = currentControllerUID;
        this.controllerSettings = controllerSettings;
        this.globalSettings = globalSettings;
    }

    public static ControlifySettings fromDTO(ControlifyConfig dto) {
        return new ControlifySettings(
                dto.currentControllerUID().orElse(null),
                ControllerSettings.fromDTO(dto.controllerConfig()),
                GlobalSettings.fromDTO(dto.globalConfig())
        );
    }

    public ControlifyConfig toDTO() {
        return new ControlifyConfig(
                Optional.ofNullable(currentControllerUID),
                controllerSettings.toDTO(),
                globalSettings.toDTO()
        );
    }
}
