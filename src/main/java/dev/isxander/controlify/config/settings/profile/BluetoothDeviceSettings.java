/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.settings.profile;

import dev.isxander.controlify.config.dto.profile.BluetoothDeviceConfig;

public class BluetoothDeviceSettings {
	public boolean dontShowWarning;

	public BluetoothDeviceSettings(boolean dontShowWarning) {
		this.dontShowWarning = dontShowWarning;
	}

	public static BluetoothDeviceSettings fromDTO(BluetoothDeviceConfig dto) {
		return new BluetoothDeviceSettings(dto.dontShowWarning());
	}

	public BluetoothDeviceConfig toDTO() {
		return new BluetoothDeviceConfig(dontShowWarning);
	}
}
