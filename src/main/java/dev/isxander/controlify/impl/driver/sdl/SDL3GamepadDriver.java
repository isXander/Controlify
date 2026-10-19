/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver.sdl;

import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.impl.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.impl.controller.gyro.GyroComponent;
import dev.isxander.controlify.impl.controller.touchpad.Touchpads;
import dev.isxander.controlify.impl.controller.input.InputComponent;
import dev.isxander.controlify.impl.input.SensorType;
import dev.isxander.controlify.impl.utils.event.EventSource;
import dev.isxander.controlify.impl.utils.log.ControlifyLogger;
import dev.isxander.sdl.*;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.isxander.sdl.SdlGamepad.*;

public class SDL3GamepadDriver extends SDLCommonDriver<SdlGamepadHandle> {

	private InputComponent inputComponent;
	private TouchpadComponent touchpadComponent;

	private final Set<Identifier> supportedButtons;
	private final Set<Identifier> supportedAxes;
	private final Set<SensorType> supportedSensors;

	private final boolean isGyroSupported;
	private boolean active;

	private final int numTouchpads;

	public SDL3GamepadDriver(Sdl sdl, SdlGamepadHandle ptrController, EventSource<SDLControllerEvent> eventSource, SdlJoystickId jid, ControllerType type, ControlifyLogger logger) {
		super(sdl, ptrController, jid, eventSource, type, logger);

		this.supportedButtons = IntStream.range(0, SDL_GAMEPAD_BUTTON_COUNT)
				.filter(i -> this.sdl.gamepad().SDL_GamepadHasButton(this.ptrController, i))
				.mapToObj(SDLInputConversions::getGamepadButton)
				.collect(Collectors.toSet());
		this.supportedAxes = IntStream.range(0, SDL_GAMEPAD_AXIS_COUNT)
				.filter(i -> this.sdl.gamepad().SDL_GamepadHasAxis(this.ptrController, i))
				.boxed()
				.flatMap(i -> Stream.of(SDLInputConversions.getGamepadAxis(i, true), SDLInputConversions.getGamepadAxis(i, false)))
				.collect(Collectors.toSet());
		this.supportedSensors = IntStream.range(0, SDL_SENSOR_COUNT)
				.filter(i -> this.sdl.gamepad().SDL_GamepadHasSensor(this.ptrController, i))
				.mapToObj(SDLInputConversions::getSensorType)
				.collect(Collectors.toSet());

		this.isGyroSupported = this.supportedSensors.contains(SensorType.GYROSCOPE);

		this.numTouchpads = sdl.gamepad().SDL_GetNumGamepadTouchpads(ptrController);
	}

	@Override
	public void addComponents(ControllerImpl controller) {
		super.addComponents(controller);

		controller.setComponent(
				this.inputComponent = new InputComponent(
						this.controllerType.id(),
						this.inputPipeline,
						this.supportedButtons,
						this.supportedAxes,
						this.supportedSensors,
						true
				)
		);

		if (this.isGyroSupported) {
			controller.setComponent(new GyroComponent(this.inputPipeline));
		}

		if (this.numTouchpads > 0) {
			controller.setComponent(this.touchpadComponent = new TouchpadComponent(
					new Touchpads(
							IntStream.range(0, numTouchpads)
								.mapToObj(i ->
										new Touchpads.Touchpad(
												sdl.gamepad().SDL_GetNumGamepadTouchpadFingers(ptrController, i)
										)
								).toArray(Touchpads.Touchpad[]::new)
					)
			));
		}
	}

	@Override
	public void setActive(boolean active) {
		if (this.ptrController == null) {
			throw new IllegalStateException("Tried to activate or deactivate a closed controller.");
		}
		if (this.active == active) {
			return;
		}
		this.active = active;
		if (this.isGyroSupported
				&& !this.sdl.gamepad().SDL_SetGamepadSensorEnabled(this.ptrController, SDL_SENSOR_GYRO, active)) {
			this.logger.warn("Failed to {} gyro sensor: {}", active ? "enable" : "disable", this.sdl.error().SDL_GetError());
		}
	}

	@Override
	protected void onEvent(SDLControllerEvent event) {
		super.onEvent(event);

		switch (event) {
			case SDLControllerEvent.GamepadTouchpad touchpad ->
				this.onTouchpadEvent(touchpad);

			default -> {}
		}
	}

	@Override
	public void update(ControllerImpl controller, boolean outOfFocus) {
		super.update(controller, outOfFocus);
	}

	@Override
	public void close() {
		this.setActive(false);
		super.close();
	}

	private void onTouchpadEvent(SDLControllerEvent.GamepadTouchpad event) {
		// TODO: touchpad component probably needs redoing, it was designed to receive
		//       complete snapshots of touchpad data, not continuous updates
		// TODO: touchpad events should probably be fed down the physical input pipeline
		//       so we can recognise gestures from finger swipes
	}

	@Override
	protected SdlPropertiesId SDL_GetControllerProperties(SdlGamepadHandle ptrController) {
		return sdl.gamepad().SDL_GetGamepadProperties(ptrController);
	}

	@Override
	protected String SDL_GetControllerName(SdlGamepadHandle ptrController) {
		return sdl.gamepad().SDL_GetGamepadName(ptrController);
	}

	@Override
	protected SdlGuid SDL_GetControllerGUIDForID(SdlJoystickId jid) {
		return sdl.gamepad().SDL_GetGamepadGUIDForID(jid);
	}

	@Override
	protected String SDL_GetControllerSerial(SdlGamepadHandle ptrController) {
		return sdl.gamepad().SDL_GetGamepadSerial(ptrController);
	}

	@Override
	protected short SDL_GetControllerVendor(SdlGamepadHandle ptrController) {
		return sdl.gamepad().SDL_GetGamepadVendor(ptrController);
	}

	@Override
	protected short SDL_GetControllerProduct(SdlGamepadHandle ptrController) {
		return sdl.gamepad().SDL_GetGamepadProduct(ptrController);
	}

	@Override
	protected int SDL_GetControllerConnectionState(SdlGamepadHandle ptrController) {
		return sdl.gamepad().SDL_GetGamepadConnectionState(ptrController);
	}

	@Override
	protected boolean SDL_CloseController(SdlGamepadHandle ptrController) {
		sdl.gamepad().SDL_CloseGamepad(ptrController);
		return true;
	}

	@Override
	protected boolean SDL_RumbleController(SdlGamepadHandle ptrController, float strong, float weak, int durationMs) {
		return sdl.gamepad().SDL_RumbleGamepad(ptrController, (short) (strong * 0xFFFF), (short) (weak * 0xFFFF), durationMs);
	}

	@Override
	protected boolean SDL_RumbleControllerTriggers(SdlGamepadHandle ptrController, float left, float right, int durationMs) {
		return sdl.gamepad().SDL_RumbleGamepadTriggers(ptrController, (short) (left * 0xFFFF), (short) (right * 0xFFFF), durationMs);
	}

	@Override
	protected int SDL_GetControllerPowerInfo(SdlGamepadHandle ptrController, SdlRefs.IntRef percent) {
		return sdl.gamepad().SDL_GetGamepadPowerInfo(ptrController, percent);
	}

	@Override
	protected boolean SDL_SendControllerEffect(SdlGamepadHandle ptrController, ByteBuffer effect) {
		return sdl.gamepad().SDL_SendGamepadEffect(ptrController, effect);
	}

	@Override
	protected boolean SDL_SetControllerLED(SdlGamepadHandle ptrController, byte red, byte green, byte blue) {
		return sdl.gamepad().SDL_SetGamepadLED(ptrController, red, green, blue);
	}
}
