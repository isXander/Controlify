package dev.isxander.controlify.config.settings;

import dev.isxander.controlify.config.dto.ControlifyConfig;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.ControllerUID;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlifySettings {
    private final List<ProfileSettings> profileSettings;
    private final GlobalSettings globalSettings;
    private final Map<ControllerUID, DeviceSettings> deviceSettings;

    private ControlifySettings() {
        this.profileSettings = new ArrayList<>();
        this.globalSettings = GlobalSettings.defaults();
        this.deviceSettings = new HashMap<>();
    }

    public ControlifySettings(
            List<ProfileSettings> controllerSettings,
            GlobalSettings globalSettings,
            Map<ControllerUID, DeviceSettings> deviceSettings
    ) {
        this.profileSettings = new ArrayList<>(controllerSettings);
        this.globalSettings = globalSettings;
        this.deviceSettings = new HashMap<>(deviceSettings);
    }

    public static ControlifySettings defaults() {
        return new ControlifySettings();
    }

    public GlobalSettings globalSettings() {
        return this.globalSettings;
    }

    public ProfileSettings getProfileSettings(int profileIndex) {
        if (profileIndex < 0 || profileIndex >= profileSettings.size()) {
            return null;
        }
        return profileSettings.get(profileIndex);
    }

    public ProfileSettings getOrCreateProfileSettings(int playerIndex, Identifier controllerType) {
        var settings = this.getProfileSettings(playerIndex);
        if (settings == null) {
            settings = ProfileSettings.createDefault(controllerType);
            this.profileSettings.add(settings);
        }
        return settings;
    }

    public DeviceSettings getOrCreateDeviceSettings(ControllerUID uid) {
        return deviceSettings.computeIfAbsent(uid, id -> DeviceSettings.defaults());
    }

    public static ControlifySettings fromDTO(ControlifyConfig dto) {
        return new ControlifySettings(
                dto.profileConfig()
                        .stream()
                        .map(ProfileSettings::fromDTO)
                        .toList(),
                GlobalSettings.fromDTO(dto.globalConfig()),
                dto.deviceConfig().entrySet()
                        .stream()
                        .collect(
                                HashMap::new,
                                (map, entry) -> map.put(entry.getKey(), DeviceSettings.fromDTO(entry.getValue())),
                                HashMap::putAll
                        )
        );
    }

    public ControlifyConfig toDTO() {
        return new ControlifyConfig(
                profileSettings
                        .stream()
                        .map(ProfileSettings::toDTO)
                        .toList(),
                globalSettings.toDTO(),
                deviceSettings.entrySet()
                        .stream()
                        .collect(
                                HashMap::new,
                                (map, entry) -> map.put(entry.getKey(), entry.getValue().toDTO()),
                                HashMap::putAll
                        )
        );
    }
}
