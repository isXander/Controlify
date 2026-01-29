package dev.isxander.controlify.config.settings;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.config.dto.ControlifyConfig;
import dev.isxander.controlify.config.settings.controller.ControllerSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ControlifySettings {
    public @Nullable String currentControllerUID;
    private List<ControllerSettings> controllerSettings;
    private GlobalSettings globalSettings;

    private ControlifySettings() {
        this.globalSettings = GlobalSettings.defaults();
    }

    public ControlifySettings(
            @Nullable String currentControllerUID,
            List<ControllerSettings> controllerSettings,
            GlobalSettings globalSettings
    ) {
        this.currentControllerUID = currentControllerUID;
        this.controllerSettings = controllerSettings;
        this.globalSettings = globalSettings;
    }

    public static ControlifySettings defaults() {
        return new ControlifySettings();
    }

    public void setCurrentControllerUID(@Nullable String uid) {
        this.currentControllerUID = uid;
    }

    public @Nullable String currentControllerUid() {
        return this.currentControllerUID;
    }

    public GlobalSettings globalSettings() {
        return this.globalSettings;
    }

    public ControllerSettings getControllerSettings(int playerIndex) {
        return controllerSettings.get(playerIndex);
    }

    public ControllerSettings getControllerSettings() {
        return getControllerSettings(0);
    }

    public ControllerSettings getControllerSettings(ControllerEntity controller) {
        var settings = this.getControllerSettings();
        if (settings == null) {
            var dto = Controlify.instance()
                    .defaultConfigManager()
                    .getDefaultForNamespace(controller.info().type().namespace());
            settings = ControllerSettings.fromDTO(dto);
            this.controllerSettings.add(settings);
        }
        return settings;
    }

    public static ControlifySettings fromDTO(ControlifyConfig dto) {
        return new ControlifySettings(
                dto.currentControllerUID().orElse(null),
                dto.controllerConfig()
                        .stream()
                        .map(ControllerSettings::fromDTO)
                        .toList(),
                GlobalSettings.fromDTO(dto.globalConfig())
        );
    }

    public ControlifyConfig toDTO() {
        return new ControlifyConfig(
                Optional.ofNullable(currentControllerUID),
                controllerSettings
                        .stream()
                        .map(ControllerSettings::toDTO)
                        .toList(),
                globalSettings.toDTO()
        );
    }
}
