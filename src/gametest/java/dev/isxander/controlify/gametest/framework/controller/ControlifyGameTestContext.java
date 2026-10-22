/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework.controller;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.impl.config.settings.GlobalSettings;
import dev.isxander.controlify.impl.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controllermanager.ControllerManager;
import dev.isxander.controlify.gametest.mixin.ControlifySettingsAccessor;
import dev.isxander.controlify.gametest.mixin.SDLControllerBackendAccessor;
import dev.isxander.sdl.Sdl;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ControlifyGameTestContext {
	private final ClientGameTestContext context;
	private final Sdl sdl;

	public ControlifyGameTestContext(ClientGameTestContext context) {
		this.context = context;
		this.sdl = ((SDLControllerBackendAccessor) Controlify.instance().getControllerCoordinator().orElseThrow().backend()).controlify_test$getSdl();
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
				.map(ControllerManager::getAllControllers)
				.orElseGet(List::of);
			var connectedUids = connectedControllers.stream()
				.map(ControllerImpl::uid)
				.collect(Collectors.toSet());

			// Keep the existing objects for connected devices because input
			// components cache their DeviceSettings reference when attached.
			var deviceSettings = settingsAccessor.controlify_test$getDeviceSettings();
			deviceSettings.keySet().retainAll(connectedUids);
			for (ControllerImpl controller : connectedControllers) {
				var device = settings.getOrCreateDeviceSettings(controller.uid());
				device.name = controller.name();
				device.lastSeen = System.currentTimeMillis();
				device.controllerType = controller.info().type().id();
				device.gyroCalibration.offset = new Vector3f();
				device.mapping = null;

				controller.setSettings(profileDefaults);
				controller.input().reloadActions();
				bindDefaultInputs(controller);
			}

			controlify.applyControllerSelection(false);
			config.markDirty();
		});
	}

	static void bindDefaultInputs(ControllerImpl controller) {
		// New profiles can leave actions unbound; each virtual device test starts with its default layout.
		controller.input().getAllActions().forEach(action -> {
			action.setBoundInput(action.defaultInput());
			if (action.defaultInput() != null) {
				controller.settings().input.bindings.bindings.put(action.spec().id(), action.defaultInput());
			}
		});
	}

}
