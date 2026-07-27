/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.settings;

import dev.isxander.controlify.config.dto.ControlifyConfig;
import dev.isxander.controlify.config.dto.SharedConfig;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMaps;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ControlifySettings {
	private final Int2ObjectSortedMap<ProfileSettings> profileSettings;
	private GlobalSettings globalSettings;
	private final Map<String, DeviceSettings> deviceSettings;

	private ControlifySettings() {
		this.profileSettings = new Int2ObjectAVLTreeMap<>();
		this.globalSettings = GlobalSettings.defaults();
		this.deviceSettings = new HashMap<>();
	}

	public static ControlifySettings defaults() {
		return new ControlifySettings();
	}

	public GlobalSettings globalSettings() {
		return this.globalSettings;
	}

	public Map<String, DeviceSettings> deviceSettings() {
		return Map.copyOf(this.deviceSettings);
	}

	public Int2ObjectSortedMap<ProfileSettings> profileSettings() {
		return Int2ObjectSortedMaps.unmodifiable(this.profileSettings);
	}

	public @Nullable ProfileSettings getProfileSettings(int profileIndex) {
		return this.profileSettings.get(profileIndex);
	}

	public void putProfileSettings(int profileIndex, ProfileSettings settings) {
		if (profileIndex < 0) {
			throw new IllegalArgumentException("Profile index must be non-negative");
		}
		this.profileSettings.put(profileIndex, settings);
	}

	public void removeProfileSettings(int profileIndex) {
		this.profileSettings.remove(profileIndex);
	}

	public DeviceSettings getOrCreateDeviceSettings(String uid) {
		return deviceSettings.computeIfAbsent(uid, DeviceSettings::defaults);
	}

	public static ControlifySettings fromLegacyDTO(ControlifyConfig dto) {
		ControlifySettings settings = fromSharedDTO(new SharedConfig(dto.globalConfig(), dto.deviceConfig()));
		for (int i = 0; i < dto.profileConfig().size(); i++) {
			settings.putProfileSettings(i, ProfileSettings.fromDTO(dto.profileConfig().get(i)));
		}
		return settings;
	}

	public static ControlifySettings fromSharedDTO(SharedConfig dto) {
		ControlifySettings settings = defaults();
		settings.globalSettings = GlobalSettings.fromDTO(dto.globalConfig());
		dto.deviceConfig().forEach((uid, config) -> {
			DeviceSettings device = DeviceSettings.fromDTO(config);
			if (device.name.isBlank()) {
				device.name = uid;
			}
			settings.deviceSettings.put(uid, device);
		});
		return settings;
	}

	public SharedConfig toSharedDTO() {
		return new SharedConfig(
				globalSettings.toDTO(),
				deviceSettings.entrySet().stream().collect(
						HashMap::new,
						(map, entry) -> map.put(entry.getKey(), entry.getValue().toDTO()),
						HashMap::putAll
				)
		);
	}
}
