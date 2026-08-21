/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework.controller;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.config.settings.GlobalSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.gametest.mixin.ControlifySettingsAccessor;
import dev.isxander.controlify.gametest.mixin.SDLControllerManagerAccessor;
import dev.isxander.sdl.Sdl;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ControlifyGameTestContext {
	private final ClientGameTestContext context;
	private final Sdl sdl;

	public ControlifyGameTestContext(ClientGameTestContext context) {
		this.context = context;
		this.sdl = ((SDLControllerManagerAccessor) Controlify.instance().getControllerManager().orElseThrow()).controlify_test$getSdl();
	}

	public VirtualControllerBuilder virtualControllerBuilder() {
		return new VirtualControllerBuilder(this.context, this, this.sdl);
	}

	public void setInputMode(InputMode inputMode) {
		Controlify.instance().setInputMode(inputMode);
		context.waitTick();
	}

	public void resetSettings() {
		context.runOnClient(_ -> {
			Controlify controlify = Controlify.instance();
			var config = controlify.config();
			var settings = config.getSettings();
			var settingsAccessor = (ControlifySettingsAccessor) settings;

			int activeProfileIndex = config.getActiveProfileIndex();
			if (activeProfileIndex < 0) {
				throw new IllegalStateException("Controlify has no active profile");
			}

			GlobalSettings globalDefaults = GlobalSettings.defaults();
			// The profile lock stays on the active index, so keep the preferred
			// profile consistent with it rather than blindly restoring index zero.
			globalDefaults.preferredProfile = activeProfileIndex;
			settingsAccessor.controlify_test$setGlobalSettings(globalDefaults);

			ProfileSettings profileDefaults = ProfileSettings.createDefault();
			settingsAccessor.controlify_test$getProfileSettings().clear();
			settings.putProfileSettings(activeProfileIndex, profileDefaults);

			var connectedControllers = controlify.getControllerManager()
				.map(ControllerManager::getConnectedControllers)
				.orElseGet(List::of);
			var connectedUids = connectedControllers.stream()
				.map(ControllerEntity::uid)
				.collect(Collectors.toSet());

			// Keep the existing objects for connected devices because input
			// components cache their DeviceSettings reference when attached.
			var deviceSettings = settingsAccessor.controlify_test$getDeviceSettings();
			deviceSettings.keySet().retainAll(connectedUids);
			for (ControllerEntity controller : connectedControllers) {
				var device = settings.getOrCreateDeviceSettings(controller.uid());
				device.name = controller.name();
				device.lastSeen = System.currentTimeMillis();
				device.controllerType = controller.info().type().namespace();
				device.gyroCalibration.offset = new GyroState();
				device.mapping = null;

				controller.setSettings(profileDefaults);
			}

			controlify.applyControllerSelection(false);
			config.markDirty();
		});
	}
}
