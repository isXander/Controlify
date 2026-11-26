/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.battery;

import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

public class BatteryLevelComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("battery_level");

	private PowerState batteryLevel = new PowerState.Unknown();

	public BatteryLevelComponent() {
		super(ID);
	}

	public PowerState getBatteryLevel() {
		return this.batteryLevel;
	}

	public void setBatteryLevel(PowerState batteryLevel) {
		this.batteryLevel = batteryLevel;
	}
}
