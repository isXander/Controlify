/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.ControlifyGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/// Ensures that {@link CTestUtil#resetControlifySettings(ClientGameTestContext)}
/// works appropriately, not leaving behind any stale references.
@SuppressWarnings("UnstableApiUsage")
public class ResetControlifySettingsTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		var controlifyContext = new ControlifyGameTestContext(context);

		try (var controller = controlifyContext.virtualControllerBuilder()
				.withXbox()
				.attach()) {
			var controlify = Controlify.instance();
			var controllerEntity = controller.getControllerEntity();
			var settings = controlify.config().getSettings();
			var cachedDeviceSettings = settings.getOrCreateDeviceSettings(controllerEntity.uid());

			cachedDeviceSettings.gyroCalibration.offset = new GyroState(1f, 2f, 3f);
			settings.globalSettings().mixedInput = true;

			CTestUtil.resetControlifySettings(context);

			if (settings.globalSettings().mixedInput) {
				throw new AssertionError("Global settings were not reset");
			}
			if (cachedDeviceSettings != settings.getOrCreateDeviceSettings(controllerEntity.uid())) {
				throw new AssertionError("Connected controller device settings reference was replaced");
			}
			if (!cachedDeviceSettings.gyroCalibration.offset.equals(new GyroState())) {
				throw new AssertionError("Device settings were not reset");
			}
			if (controllerEntity.settings() != controlify.config().getActiveProfile()) {
				throw new AssertionError("Controller was not rebound to the default active profile");
			}
		}
	}
}
