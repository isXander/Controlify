/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controllermanager;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.config.ConfigManager;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.driver.sdl.SDLNativesLoader;
import dev.isxander.controlify.gui.screen.AskToMapControllerScreen;
import dev.isxander.controlify.gui.screen.BluetoothWarningScreen;
import dev.isxander.controlify.input.physical.PhysicalInputSignal;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.DebugLog;
import dev.isxander.controlify.utils.MinecraftUtil;
import dev.isxander.controlify.utils.SetupWizard;
import dev.isxander.controlify.utils.SetupWizardManager;
import dev.isxander.controlify.utils.event.EventSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.isxander.controlify.utils.ControllerUtils.wrapControllerError;

/// Owns controller discovery, selection, activation and activity subscriptions.
/// Publishes selection changes through {@link ControlifyEvents#CURRENT_CONTROLLER_CHANGED}.
public class ControllerCoordinator implements AutoCloseable {
	private final Controlify controlify;
	private final ConfigManager config;
	private final SetupWizardManager setupWizardManager;
	private final ControllerManager controllerManager = new ControllerManager();
	private final Map<ControllerEntity, EventSource.Subscription> inputSubscriptions = new HashMap<>();
	private final Set<ControllerEntity> controllersWithInput = new LinkedHashSet<>();

	private @Nullable ControllerBackend backend;
	private @Nullable ControllerEntity currentController;
	private boolean hasDiscoveredControllers;
	private boolean closed;

	public ControllerCoordinator(Controlify controlify, SetupWizardManager setupWizardManager) {
		this.controlify = controlify;
		this.config = controlify.config();
		this.setupWizardManager = setupWizardManager;

		ControlifyEvents.CONTROLLER_CONNECTED.register(event ->
				this.onControllerAdded(event.controller(), event.hotplugged()));
		ControlifyEvents.CONTROLLER_DISCONNECTED.register(event ->
				this.onControllerRemoved(event.controller()));
	}

	public boolean initialize() {
		try {
			this.backend = new SDLControllerBackend(SDLNativesLoader.load(), this.controlify, this.controllerManager);
		} catch (Throwable throwable) {
			CUtil.LOGGER.error("Failed to initialize controller backend", throwable);
			return false;
		}
		this.backend.setup();
		return true;
	}

	public boolean isReady() {
		return this.backend != null && !this.closed;
	}

	public ControllerManager controllerManager() {
		return this.controllerManager;
	}

	public Optional<ControllerEntity> currentController() {
		return Optional.ofNullable(this.currentController);
	}

	public void discoverControllers() {
		if (!this.isReady()) {
			return;
		}
		if (this.hasDiscoveredControllers) {
			CUtil.LOGGER.warn("Attempted to discover controllers twice!");
			return;
		}
		this.hasDiscoveredControllers = true;
		DebugLog.log("Discovering and initializing controllers...");
		this.backend.discoverControllers();
		if (this.controllerManager.getAllControllers().isEmpty()) {
			CUtil.LOGGER.log("No controllers found.");
		}
		this.applyControllerSelection(false);
		this.config.saveIfDirty();

		PlatformMainUtil.applyToControlifyEntrypoint(entrypoint -> {
			try {
				entrypoint.onControllersDiscovered(this.controlify);
			} catch (Throwable e) {
				CUtil.LOGGER.error("Failed to run `onControllersDiscovered` on Controlify entrypoint: {}", entrypoint.getClass().getName(), e);
			}
		});
	}

	/// Polls every controller and resolves selection after all events have been received.
	/// @return whether the selected controller received non-sensor input during this poll
	public boolean update(boolean outOfFocus) {
		this.controllersWithInput.clear();

		if (!this.isReady()) {
			return false;
		}

		this.backend.pollEvents();

		if (!outOfFocus && this.config.getActiveProfile().controllerUid == null
				&& !this.controllersWithInput.contains(this.currentController)) {
			// The active controller wins regardless of the order of events within the poll.
			this.controllersWithInput.stream().findFirst().ifPresent(controller -> wrapControllerError(
					() -> this.setCurrentController(controller, true),
					"Selecting controller from input", controller));
		}
		return !outOfFocus && this.controllersWithInput.contains(this.currentController);
	}

	public void setCurrentController(@Nullable ControllerEntity controller, boolean changeInputMode) {
		if (this.closed || this.currentController == controller) {
			return;
		}
		if (this.currentController != null) {
			this.currentController.setActive(false);
		}
		this.currentController = controller;
		if (controller != null) {
			controller.setActive(true);
			DebugLog.log("Updated current controller to {}({})", controller.name(), controller.uid());
		} else {
			DebugLog.log("Cleared current controller.");
		}
		ControlifyEvents.CURRENT_CONTROLLER_CHANGED.invoke(
				new ControlifyEvents.CurrentControllerChanged(controller, changeInputMode));
	}

	public void applyControllerSelection(boolean changeInputMode) {
		String selectedUid = this.config.getActiveProfile().controllerUid;
		ControllerEntity selected = this.controllerManager.getAllControllers().stream()
				.filter(controller -> selectedUid == null || selectedUid.equals(controller.uid()))
				.findFirst().orElse(null);
		this.setCurrentController(selected, changeInputMode);
	}

	public void applyActiveProfile() {
		this.controllerManager.getAllControllers().forEach(controller ->
				controller.setSettings(this.config.getActiveProfile()));
		this.applyControllerSelection(true);
	}

	private void onControllerAdded(ControllerEntity controller, boolean hotplugged) {
		if (this.closed) {
			return;
		}
		controller.input().ifPresent(input -> {
			EventSource.Subscription previous = this.inputSubscriptions.remove(controller);
			if (previous != null) {
				previous.close();
			}
			this.inputSubscriptions.put(controller, input.inputPipeline().sourceProcessedPhysicalSignals()
					.filter(signal -> signal instanceof PhysicalInputSignal)
					.subscribe(_ -> this.controllersWithInput.add(controller)));
		});

		DeviceSettings rememberedDevice = this.config.getSettings().getOrCreateDeviceSettings(controller.uid());
		rememberedDevice.name = controller.name();
		rememberedDevice.lastSeen = System.currentTimeMillis();
		rememberedDevice.controllerType = controller.info().type().namespace();
		this.config.markDirty();

		String selectedUid = this.config.getActiveProfile().controllerUid;
		if (selectedUid == null || selectedUid.equals(controller.uid())) {
			this.setCurrentController(controller, true);
		}

		SetupWizard wizard = new SetupWizard();
		wizard.addStage(
				() -> controller.input().map(input -> !input.isGamepad()
						&& this.config.getSettings().getOrCreateDeviceSettings(controller.uid()).mapping == null)
						.orElse(false),
				nextScreen -> new AskToMapControllerScreen(controller, nextScreen));
		wizard.addStage(
				() -> controller.dualSense().isPresent()
						&& controller.bluetooth().map(bt -> !bt.settings().dontShowWarning).orElse(false),
				nextScreen -> new BluetoothWarningScreen(controller.bluetooth().orElseThrow(), nextScreen));
		if (hotplugged) {
			MinecraftUtil.sendToast(
					Component.translatable("controlify.toast.controller_connected.title"),
					Component.translatable("controlify.toast.controller_connected.description", controller.name()), false);
			this.config.saveIfDirty();
		}
		this.setupWizardManager.push(wizard);
	}

	private void onControllerRemoved(ControllerEntity controller) {
		if (this.closed) {
			return;
		}
		EventSource.Subscription subscription = this.inputSubscriptions.remove(controller);
		if (subscription != null) {
			subscription.close();
		}
		this.controllersWithInput.remove(controller);
		if (this.currentController == controller) {
			ControllerEntity fallback = this.config.getActiveProfile().controllerUid == null
					? this.controllerManager.getAllControllers().stream()
							.filter(candidate -> candidate != controller).findFirst().orElse(null)
					: null;
			this.setCurrentController(fallback, true);
		}
		MinecraftUtil.sendToast(
				Component.translatable("controlify.toast.controller_disconnected.title"),
				Component.translatable("controlify.toast.controller_disconnected.description", controller.name()), false);
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
		this.inputSubscriptions.values().forEach(EventSource.Subscription::close);
		this.inputSubscriptions.clear();
		this.controllersWithInput.clear();
		this.currentController = null;
		this.controllerManager.close();
	}
}
