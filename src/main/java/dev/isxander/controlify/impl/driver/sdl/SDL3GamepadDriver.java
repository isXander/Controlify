/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver.sdl;

import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.impl.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.impl.controller.gyro.GyroComponentImpl;
import dev.isxander.controlify.impl.controller.touchpad.Touchpads;
import dev.isxander.controlify.impl.controller.capsense.CapSenseComponent;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import dev.isxander.controlify.impl.input.mapping.GamepadInputs;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.utils.log.ControlifyLogger;
import dev.isxander.sdl.*;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.isxander.sdl.SdlGamepad.*;

public class SDL3GamepadDriver extends SDLCommonDriver<SdlGamepadHandle> {

	private InputComponentImpl inputComponent;
	private TouchpadComponent touchpadComponent;

	private final Set<Identifier> supportedButtons;
	private final Set<Identifier> supportedAxes;
	private final Set<Identifier> supportedCapSensors;

	private boolean seededCapSense;

	private final int numTouchpads;

	public SDL3GamepadDriver(Sdl sdl, SdlGamepadHandle ptrController, EventSource<SDLControllerEvent> eventSource, SdlJoystickId jid, ControllerType type, ControlifyLogger logger) {
		super(sdl, ptrController, jid, eventSource, type, logger);

		this.numTouchpads = sdl.gamepad().SDL_GetNumGamepadTouchpads(ptrController);

		this.supportedButtons = IntStream.range(0, SDL_GAMEPAD_BUTTON_COUNT)
				.filter(i -> this.sdl.gamepad().SDL_GamepadHasButton(this.ptrController, i))
				.mapToObj(SDLInputConversions::getGamepadButton)
				.collect(Collectors.toSet());
		this.supportedAxes = IntStream.range(0, SDL_GAMEPAD_AXIS_COUNT)
				.filter(i -> this.sdl.gamepad().SDL_GamepadHasAxis(this.ptrController, i))
				.boxed()
				.flatMap(i -> Stream.of(SDLInputConversions.getGamepadAxis(i, true), SDLInputConversions.getGamepadAxis(i, false)))
				.collect(Collectors.toSet());

		this.supportedCapSensors = Stream.concat(
				IntStream.range(0, SDL_GAMEPAD_CAPSENSE_COUNT)
						.filter(i -> this.sdl.gamepad().SDL_GamepadHasCapSense(this.ptrController, i))
						.mapToObj(SDLInputConversions::getGamepadCapSense)
						.filter(Objects::nonNull),
				IntStream.range(0, this.numTouchpads).mapToObj(GamepadInputs::touchpadCapSense)
		).collect(Collectors.toSet());
	}

	@Override
	public void addComponents(ControllerImpl controller) {
		super.addComponents(controller);

		controller.addComponent(
				this.inputComponent = new InputComponentImpl(
						this.controllerType.id(),
						this.inputPipeline,
						this.supportedButtons,
						this.supportedAxes,
						true
				)
		);

		if (!this.supportedCapSensors.isEmpty()) {
			controller.addComponent(new CapSenseComponent(this.inputPipeline, this.supportedCapSensors));
		}

		if (this.numTouchpads > 0) {
			controller.addComponent(this.touchpadComponent = new TouchpadComponent(
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
	protected void onEvent(SDLControllerEvent event) {
		// filter out joystick input events if we are a gamepad
		if (event instanceof SDLControllerEvent.JoystickEvent && !(event instanceof SDLControllerEvent.DeviceEvent)) {
			return;
		}

		// SDL only sends capsense events on change, so a thumb already resting on a stick
		// at connect time would be missed. Seed the initial state on the first frame,
		// using the frame's timestamp to stay on the same clock as other signals.
		if (!this.seededCapSense && event instanceof SDLControllerEvent.Frame frame) {
			this.seededCapSense = true;
			for (int i = 0; i < SDL_GAMEPAD_CAPSENSE_COUNT; i++) {
				Identifier id = SDLInputConversions.getGamepadCapSense(i);
				if (id != null && this.supportedCapSensors.contains(id)
						&& this.sdl.gamepad().SDL_GetGamepadCapSense(this.ptrController, i)) {
					this.inputPipeline.accept(new PhysicalInputSignal.CapSenseChanged(frame.timeNanos(), id, true));
				}
			}
		}

		super.onEvent(event);

		switch (event) {
			case SDLControllerEvent.GamepadTouchpad touchpad ->
				this.onTouchpadEvent(touchpad);

			default -> {}
		}
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

	@Override
	protected boolean SDL_ControllerHasSensor(SdlGamepadHandle ptrController, int type) {
		return sdl.gamepad().SDL_GamepadHasSensor(ptrController, type);
	}

	@Override
	protected boolean SDL_SetControllerSensorEnabled(SdlGamepadHandle ptrController, int type, boolean enabled) {
		return sdl.gamepad().SDL_SetGamepadSensorEnabled(ptrController, type, enabled);
	}
}
