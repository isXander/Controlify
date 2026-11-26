/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import org.joml.Vector3f;

/// Ensures that {@link ControlifyGameTestContext#resetSettings()}
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
			var ControllerImpl = controller.getControllerImpl();
			var settings = controlify.config().getSettings();
			var cachedDeviceSettings = settings.getOrCreateDeviceSettings(ControllerImpl.uid());

			cachedDeviceSettings.gyroCalibration.offset = new Vector3f(1f, 2f, 3f);
			settings.globalSettings().mixedInput = true;

			controlifyContext.resetSettings();

			if (settings.globalSettings().mixedInput) {
				throw new AssertionError("Global settings were not reset");
			}
			if (cachedDeviceSettings != settings.getOrCreateDeviceSettings(ControllerImpl.uid())) {
				throw new AssertionError("Connected controller device settings reference was replaced");
			}
			if (!cachedDeviceSettings.gyroCalibration.offset.equals(new Vector3f())) {
				throw new AssertionError("Device settings were not reset");
			}
			if (ControllerImpl.settings() != controlify.config().getActiveProfile()) {
				throw new AssertionError("Controller was not rebound to the default active profile");
			}
		}
	}
}
