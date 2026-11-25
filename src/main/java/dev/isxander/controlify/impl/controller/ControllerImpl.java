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
import dev.isxander.controlify.impl.controller.capsense.CapSenseComponent;
import dev.isxander.controlify.impl.controller.dualsense.DualsenseComponentImpl;
import dev.isxander.controlify.impl.controller.gyro.GyroComponentImpl;
import dev.isxander.controlify.impl.controller.haptic.PcmHapticsComponent;
import dev.isxander.controlify.impl.controller.haptic.SteamHapticsComponent;
import dev.isxander.controlify.impl.feedback.SteamHapticsRenderer;
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
import dev.isxander.controlify.impl.feedback.DualMotorRenderer;
import dev.isxander.controlify.impl.feedback.FeedbackEngine;
import dev.isxander.controlify.impl.feedback.FeedbackRenderer;
import dev.isxander.controlify.impl.feedback.LightRenderer;
import dev.isxander.controlify.impl.feedback.LiveFeedbackServices;
import dev.isxander.controlify.impl.feedback.PcmRenderer;
import dev.isxander.controlify.impl.feedback.SwitchingRenderer;
import dev.isxander.controlify.impl.feedback.TriggerMotorRenderer;
import dev.isxander.controlify.impl.utils.log.ControlifyLogger;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public final class ControllerImpl implements Controller {
	private final ControllerInfo info;
	private final Driver driver;
	private final ControlifyLogger logger;

	private final Map<Identifier, ControllerComponent> components;
	private final FeedbackEngine feedback;

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
		this.feedback = this.createFeedbackEngine();

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

	/// The feedback engine of this controller, which mixes every playing effect and drives the renderers.
	public @NonNull FeedbackEngine feedback() {
		return this.feedback;
	}

	private FeedbackEngine createFeedbackEngine() {
		List<FeedbackRenderer> renderers = new ArrayList<>();
		SteamHapticsComponent steam = this.steamHaptics().orElse(null);
		BooleanSupplier steamHdActive = () -> steam != null && steam.settings().enabled && steam.isWorking();

		FeedbackRenderer motors = this.rumble()
				.<FeedbackRenderer>map(rumble -> new DualMotorRenderer(rumble::queueRumble, steamHdActive))
				.orElse(null);
		PcmHapticsComponent pcmHaptics = this.pcmHaptics().orElse(null);
		if (pcmHaptics != null) {
			// the HD actuators also emulate rumble, so only ever drive them one way at a time
			FeedbackRenderer pcm = new PcmRenderer(pcmHaptics.sink());
			renderers.add(motors == null ? pcm : new SwitchingRenderer(() -> pcmHaptics.settings().enabled, pcm, motors));
		} else if (motors != null) {
			renderers.add(motors);
		}

		// voices and clicks only; this controller's rumble still goes through the motors above
		if (steam != null) {
			renderers.add(new SwitchingRenderer(steamHdActive, new SteamHapticsRenderer(steam), FeedbackRenderer.NONE));
		}

		this.triggerRumble().ifPresent(triggers -> renderers.add(new TriggerMotorRenderer(triggers::queueTriggerRumble)));
		this.led().ifPresent(led -> renderers.add(new SwitchingRenderer(
				() -> this.settings().light.enabled,
				new LightRenderer(led::setColor, LEDComponent.DEFAULT_COLOR),
				FeedbackRenderer.NONE
		)));

		return new FeedbackEngine(new LiveFeedbackServices(this), renderers);
	}

	@Override
	public @NonNull InputComponentImpl input() {
		// assert that all controllers must have InputComponentImpl
		return this.<InputComponentImpl>getComponent(InputComponentImpl.ID).orElseThrow();
	}

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

	public @NonNull Optional<CapSenseComponent> capSense() {
		return this.getComponent(CapSenseComponent.ID);
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

	@Override
	public @NonNull Optional<DualsenseComponentImpl> dualsense() {
		return this.getComponent(DualsenseComponentImpl.ID);
	}

	public @NonNull Optional<PcmHapticsComponent> pcmHaptics() {
		return this.getComponent(PcmHapticsComponent.ID);
	}

	public @NonNull Optional<SteamHapticsComponent> steamHaptics() {
		return this.getComponent(SteamHapticsComponent.ID);
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
			// Only the current controller has its feedback updated, so nothing would ever
			// turn off what is playing. Stop it and flush the stop to the device now.
			this.feedback.clear();
			this.driver.update(this);
		}
	}

	public void close() {
		this.setActive(false);
		this.feedback.clear();
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
