/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.misc;

import dev.isxander.controlify.impl.config.settings.profile.BluetoothDeviceSettings;
import dev.isxander.controlify.impl.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

public class BluetoothDeviceComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("bluetooth");

	public BluetoothDeviceComponent() {
		super(ID);
	}

	public BluetoothDeviceSettings settings() {
		return this.controller().settings().bluetoothDevice;
	}

	public BluetoothDeviceSettings defaultSettings() {
		return this.controller().defaultSettings().bluetoothDevice;
	}
}
