/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controllermanager;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.controllermanager.ControllerEvents;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ActionContextRegistryImpl;
import dev.isxander.controlify.impl.config.ConfigManager;
import dev.isxander.controlify.impl.config.settings.device.DeviceSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.ControllerInfo;
import dev.isxander.controlify.impl.driver.sdl.SDLNativesLoader;
import dev.isxander.controlify.impl.gui.screen.AskToMapControllerScreen;
import dev.isxander.controlify.impl.gui.screen.BluetoothWarningScreen;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.DebugLog;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.impl.utils.SetupWizard;
import dev.isxander.controlify.impl.utils.SetupWizardManager;
import dev.isxander.controlify.impl.utils.event.EventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import static dev.isxander.controlify.impl.utils.ControllerUtils.wrapControllerError;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/// Owns controller discovery, selection, activation and activity subscriptions.
/// Publishes selection changes through {@link ControllerEvents#CURRENT_CONTROLLER_CHANGED}.
public class ControllerCoordinator implements AutoCloseable {
	/// Does not initialize a coordinator, backend or any manager when addons subscribe.
	public static final class Events {
		public static final EventBus<ControlifyApi> DISCOVERY_COMPLETED = new EventBus<>();
		public static final EventBus<ControllerEvents.ControllerConnected> CONTROLLER_CONNECTED = new EventBus<>();
		public static final EventBus<ControllerEvents.ControllerDisconnected> CONTROLLER_DISCONNECTED = new EventBus<>();
		public static final EventBus<ControllerEvents.CurrentControllerChanged> CURRENT_CONTROLLER_CHANGED = new EventBus<>();

		private Events() {
		}
	}

	private final Controlify controlify;
	private final ConfigManager config;
	private final SetupWizardManager setupWizardManager;
	private final ControllerManager controllerManager;
	private final Map<ControllerImpl, EventSource.Subscription> inputSubscriptions = new HashMap<>();
	private final Set<ControllerImpl> controllersWithInput = new LinkedHashSet<>();

	private @Nullable ControllerBackend backend;
	private @Nullable ControllerImpl currentController;
	private boolean hasDiscoveredControllers;
	private boolean closed;

	private boolean requireActionPulseConsume;

	public ControllerCoordinator(Controlify controlify, SetupWizardManager setupWizardManager) {
		this.controlify = controlify;
		this.config = controlify.config();
		this.setupWizardManager = setupWizardManager;

		this.controllerManager = new ControllerManager(this::onControllerAdded, this::onControllerRemoved);
	}

	public boolean initialize() {
		try {
			this.backend = new SDLControllerBackend(SDLNativesLoader.load(), this.controlify, this.controllerManager);
			this.backend.setup();
		} catch (Throwable throwable) {
			CUtil.LOGGER.error("Failed to initialize controller backend", throwable);
			this.backend = null;
			return false;
		}
		return true;
	}

	public boolean isReady() {
		return this.backend != null && !this.closed;
	}

	public ControllerManager controllerManager() {
		return this.controllerManager;
	}

	public Optional<ControllerImpl> currentController() {
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

		Events.DISCOVERY_COMPLETED.emit(this.controlify);
	}

	/// Polls every controller and resolves selection after all events have been received.
	/// @return whether the selected controller received non-sensor input during this poll
	public boolean update(boolean outOfFocus) {
		this.controllersWithInput.clear();

		if (!this.isReady()) {
			return false;
		}

		var contexts = ActionContextRegistryImpl.INSTANCE.getApplicableContexts(Minecraft.getInstance());
		for (var controller : this.controllerManager.getAllControllers()) {
			controller.input().actionGraph().setCurrentContexts(outOfFocus ? Set.of() : contexts);
		}

		this.backend.pollEvents();

		for (var controller : this.controllerManager.getAllControllers()) {
			controller.update();
		}

		if (!outOfFocus && this.config.getActiveProfile().controllerUid == null
				&& !this.controllersWithInput.contains(this.currentController)) {
			// The active controller wins regardless of the order of events within the poll.
			this.controllersWithInput.stream().findFirst().ifPresent(controller -> wrapControllerError(
					() -> this.setCurrentController(controller, true),
					"Selecting controller from input", controller));
		}
		return !outOfFocus && this.controllersWithInput.contains(this.currentController);
	}

	public void fixedUpdate() {
		this.requireActionPulseConsume = true;
	}

	public void postUpdate() {
		// after a FixedUpdate 
		if (this.requireActionPulseConsume) {
			this.requireActionPulseConsume = false;

			for (var controller : this.controllerManager.getAllControllers()) {
				for (var action : controller.input().getAllActions()) {
					action.state().consumeAllPulses();
				}
			}
		}
	}

	public void setCurrentController(@Nullable ControllerImpl controller, boolean changeInputMode) {
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
		this.controlify.onCurrentControllerChanged(controller, changeInputMode);
		Events.CURRENT_CONTROLLER_CHANGED.emit(new ControllerEvents.CurrentControllerChanged(this.controlify, controller));
	}

	public void applyControllerSelection(boolean changeInputMode) {
		String selectedUid = this.config.getActiveProfile().controllerUid;
		ControllerImpl selected = this.controllerManager.getAllControllers().stream()
				.filter(controller -> selectedUid == null || selectedUid.equals(controller.uid()))
				.findFirst().orElse(null);
		this.setCurrentController(selected, changeInputMode);
	}

	/// Recreates type-dependent drivers and components through the normal remove/add lifecycle.
	public void reinitController(ControllerImpl controller, ControllerType newType) {
		if (!this.isReady()) {
			return;
		}
		var info = controller.info();
		if (this.controllerManager.getControllerByUcid(info.ucid()).orElse(null) != controller) {
			throw new IllegalArgumentException("Controller is not registered with this coordinator");
		}
		ControllerImpl previousSelection = this.currentController;
		InputMode previousInputMode = this.controlify.currentInputMode();
		this.controllerManager.removeController(info.ucid());
		try {
			this.backend.createAndAddController(new ControllerInfo(info.ucid(), newType, info.hid()), false);
		} finally {
			// Adding an auto-selected controller must not steal selection from another device.
			if (previousSelection == controller) {
				this.controllerManager.getControllerByUcid(info.ucid())
						.ifPresent(replacement -> this.setCurrentController(replacement, false));
			} else {
				this.setCurrentController(previousSelection, false);
			}
			this.controlify.setInputMode(previousInputMode);
		}
	}

	public void applyActiveProfile() {
		this.controllerManager.getAllControllers().forEach(controller ->
				controller.setSettings(this.config.getActiveProfile()));
		this.applyControllerSelection(true);
	}

	private void onControllerAdded(ControllerImpl controller, boolean hotplugged) {
		if (this.closed) {
			return;
		}
		EventSource.Subscription previous = this.inputSubscriptions.remove(controller);
		if (previous != null) {
			previous.close();
		}
		this.inputSubscriptions.put(controller, controller.input().inputPipeline().sourceProcessedPhysicalSignals()
				.filter(signal -> signal instanceof PhysicalInputSignal)
				.subscribe(_ -> this.controllersWithInput.add(controller)));

		DeviceSettings rememberedDevice = this.config.getSettings().getOrCreateDeviceSettings(controller.uid());
		rememberedDevice.name = controller.name();
		rememberedDevice.lastSeen = System.currentTimeMillis();
		rememberedDevice.controllerType = controller.info().type().id();
		this.config.markDirty();

		String selectedUid = this.config.getActiveProfile().controllerUid;
		if (selectedUid == null || selectedUid.equals(controller.uid())) {
			this.setCurrentController(controller, true);
		}

		SetupWizard wizard = new SetupWizard();
		wizard.addStage(
				() -> !controller.input().isGamepad()
						&& this.config.getSettings().getOrCreateDeviceSettings(controller.uid()).mapping == null,
				nextScreen -> new AskToMapControllerScreen(controller, nextScreen));
		wizard.addStage(
				() -> controller.dualsense().isPresent()
						&& controller.bluetoothDevice().map(bt -> !bt.settings().dontShowWarning).orElse(false),
				nextScreen -> new BluetoothWarningScreen(controller.bluetoothDevice().orElseThrow(), nextScreen));
		if (hotplugged) {
			MinecraftUtil.sendToast(
					Component.translatable("controlify.toast.controller_connected.title"),
					Component.translatable("controlify.toast.controller_connected.description", controller.name()), false);
			this.config.saveIfDirty();
		}
		this.setupWizardManager.push(wizard);
		Events.CONTROLLER_CONNECTED.emit(new ControllerEvents.ControllerConnected(this.controlify, controller, hotplugged));
	}

	private void onControllerRemoved(ControllerImpl controller) {
		if (this.closed) {
			return;
		}
		EventSource.Subscription subscription = this.inputSubscriptions.remove(controller);
		if (subscription != null) {
			subscription.close();
		}
		this.controllersWithInput.remove(controller);
		if (this.currentController == controller) {
			ControllerImpl fallback = this.config.getActiveProfile().controllerUid == null
					? this.controllerManager.getAllControllers().stream()
							.filter(candidate -> candidate != controller).findFirst().orElse(null)
					: null;
			this.setCurrentController(fallback, true);
		}
		MinecraftUtil.sendToast(
				Component.translatable("controlify.toast.controller_disconnected.title"),
				Component.translatable("controlify.toast.controller_disconnected.description", controller.name()), false);
		Events.CONTROLLER_DISCONNECTED.emit(new ControllerEvents.ControllerDisconnected(this.controlify, controller));
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

	public @Nullable ControllerBackend backend() {
		return this.backend;
	}
}
