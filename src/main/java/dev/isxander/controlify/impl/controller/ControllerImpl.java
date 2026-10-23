/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.impl.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.impl.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.impl.controller.dualsense.DualsenseComponent;
import dev.isxander.controlify.impl.controller.gyro.GyroComponentImpl;
import dev.isxander.controlify.impl.controller.haptic.HDHapticComponent;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import dev.isxander.controlify.impl.controller.led.LEDComponent;
import dev.isxander.controlify.impl.controller.misc.BluetoothDeviceComponent;
import dev.isxander.controlify.impl.controller.misc.DriverNameComponent;
import dev.isxander.controlify.impl.controller.misc.UIDComponent;
import dev.isxander.controlify.impl.controller.rumble.RumbleComponentImpl;
import dev.isxander.controlify.impl.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.impl.controller.steamdeck.SteamDeckComponent;
import dev.isxander.controlify.impl.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.impl.driver.Driver;
import dev.isxander.controlify.impl.utils.log.ControlifyLogger;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ControllerImpl implements Controller {
	private final ControllerInfo info;
	private final Driver driver;
	private final ControlifyLogger logger;

	private final Map<Identifier, ControllerComponent> components;

	private ProfileSettings settings;
	private ProfileSettings defaultSettings;

	private boolean active;

	public ControllerImpl(
			ControllerInfo info,
			Driver driver,
			ControlifyLogger logger,
			ProfileSettings settings,
			ProfileSettings defaultSettings
	) {
		this.info = info;
		this.driver = driver;
		this.logger = logger;
		this.components = new HashMap<>();
		this.settings = settings;
		this.defaultSettings = defaultSettings;
		this.active = false;

		this.driver.addComponents(this);
		this.getAllComponents().values().forEach(c -> c.attach(this));

		logger.debugLog(
				"Components: {}",
				this.getAllComponents()
						.keySet()
						.stream()
						.map(Identifier::toString)
						.collect(Collectors.joining(", "))
		);
	}

	@Override
	public @NonNull String uid() {
		return this.<UIDComponent>getComponent(UIDComponent.ID).orElseThrow().value();
	}

	@Override
	public @NonNull String driverReportedName() {
		return this.<DriverNameComponent>getComponent(DriverNameComponent.ID).orElseThrow().value();
	}

	@Override
	public @NonNull String name() {
		return this.info.type().friendlyName()
				.orElse(this.driverReportedName());
	}

	@Override
	public @NonNull Identifier typeId() {
		return this.info.type().id();
	}

	public @NonNull ControllerInfo info() {
		return this.info;
	}

	public @NonNull Driver driver() {
		return this.driver;
	}

	public @NonNull ProfileSettings settings() {
		return this.settings;
	}

	public void setSettings(@NonNull ProfileSettings settings) {
		this.settings = settings;
		this.input().reloadActions();
	}

	public void reloadDefaultSettings() {
		this.defaultSettings = ProfileSettings.createDefault();
	}

	public @NonNull ProfileSettings defaultSettings() {
		return this.defaultSettings;
	}

	@Override
	public @NonNull InputComponentImpl input() {
		// assert that all controllers must have InputComponentImpl
		return this.<InputComponentImpl>getComponent(InputComponentImpl.ID).orElseThrow();
	}

	@Override
	public @NonNull Optional<RumbleComponentImpl> rumble() {
		return this.getComponent(RumbleComponentImpl.ID);
	}

	public @NonNull Optional<TriggerRumbleComponent> triggerRumble() {
		return this.getComponent(TriggerRumbleComponent.ID);
	}

	@Override
	public @NonNull Optional<GyroComponentImpl> gyro() {
		return this.getComponent(GyroComponentImpl.ID);
	}

	public @NonNull Optional<LEDComponent> led() {
		return this.getComponent(LEDComponent.ID);
	}

	public @NonNull Optional<TouchpadComponent> touchpad() {
		return this.getComponent(TouchpadComponent.ID);
	}

	public @NonNull Optional<BatteryLevelComponent> batteryLevel() {
		return this.getComponent(BatteryLevelComponent.ID);
	}

	public @NonNull Optional<DualsenseComponent> dualsense() {
		return this.getComponent(DualsenseComponent.ID);
	}

	public @NonNull Optional<HDHapticComponent> hdHaptics() {
		return this.getComponent(HDHapticComponent.ID);
	}

	public @NonNull Optional<BluetoothDeviceComponent> bluetoothDevice() {
		return this.getComponent(BluetoothDeviceComponent.ID);
	}

	public @NonNull Optional<SteamDeckComponent> steamDeck() {
		return this.getComponent(SteamDeckComponent.ID);
	}

	public void update() {
		this.driver.update(this);
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		if (this.active == active) {
			return;
		}
		this.active = active;
		this.driver.setActive(active);
		if (!active) {
			this.input().resetActions();
		}
	}

	public void close() {
		this.setActive(false);
		this.getAllComponents().values().forEach(ControllerComponent::close);
		this.driver.close();
	}

	public ControlifyLogger logger() {
		return this.logger;
	}

	@SuppressWarnings("unchecked")
	private <T extends ControllerComponent> Optional<T> getComponent(Identifier id) {
		return Optional.ofNullable((T) this.components.get(id));
	}

	public boolean addComponent(ControllerComponent component) {
		return this.components.put(component.id(), component) != null;
	}

	public Map<Identifier, ControllerComponent> getAllComponents() {
		return Collections.unmodifiableMap(this.components);
	}
}
