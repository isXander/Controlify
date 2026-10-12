/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.input.JoystickInputs;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.input.InputComponent;
import dev.isxander.controlify.utils.event.EventSource;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl.*;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SDL3JoystickDriver extends SDLCommonDriver<SdlJoystickHandle> {

	private InputComponent inputComponent;

	private final int numAxes, numButtons;
	private final Set<Identifier> supportedAxes;
	private final Set<Identifier> supportedButtons;

	public SDL3JoystickDriver(Sdl sdl, SdlJoystickHandle ptrJoystick, EventSource<SDLControllerEvent> eventSource, SdlJoystickId jid, ControllerType type, ControlifyLogger logger) {
		super(sdl, ptrJoystick, jid, eventSource, type, logger);

		this.numAxes = sdl.joystick().SDL_GetNumJoystickAxes(ptrJoystick);
		this.numButtons = sdl.joystick().SDL_GetNumJoystickButtons(ptrJoystick);

		this.supportedAxes = IntStream.range(0, this.numAxes)
				.boxed()
				.flatMap(i -> Stream.of(JoystickInputs.axis(i, false), JoystickInputs.axis(i, true)))
				.collect(Collectors.toSet());
		this.supportedButtons = IntStream.range(0, this.numButtons)
				.mapToObj(JoystickInputs::button)
				.collect(Collectors.toSet());
	}

	@Override
	public void addComponents(ControllerEntity controller) {
		super.addComponents(controller);

		controller.setComponent(this.inputComponent = new InputComponent(
				this.controllerType.namespace(),
				this.inputPipeline,
				this.supportedButtons,
				this.supportedAxes,
				Set.of(),
				false,
				Set.of()
		));
	}

	@Override
	public void update(ControllerEntity controller, boolean outOfFocus) {
		super.update(controller, outOfFocus);
	}

	@Override
	protected SdlPropertiesId SDL_GetControllerProperties(SdlJoystickHandle ptrController) {
		return sdl.joystick().SDL_GetJoystickProperties(ptrController);
	}

	@Override
	protected String SDL_GetControllerName(SdlJoystickHandle ptrController) {
		return sdl.joystick().SDL_GetJoystickName(ptrController);
	}

	@Override
	protected SdlGuid SDL_GetControllerGUIDForID(SdlJoystickId jid) {
		return sdl.joystick().SDL_GetJoystickGUIDForID(jid);
	}

	@Override
	protected String SDL_GetControllerSerial(SdlJoystickHandle ptrController) {
		return sdl.joystick().SDL_GetJoystickSerial(ptrController);
	}

	@Override
	protected short SDL_GetControllerVendor(SdlJoystickHandle ptrController) {
		return sdl.joystick().SDL_GetJoystickVendor(ptrController);
	}

	@Override
	protected short SDL_GetControllerProduct(SdlJoystickHandle ptrController) {
		return sdl.joystick().SDL_GetJoystickProduct(ptrController);
	}

	@Override
	protected int SDL_GetControllerConnectionState(SdlJoystickHandle ptrController) {
		return sdl.joystick().SDL_GetJoystickConnectionState(ptrController);
	}

	@Override
	protected boolean SDL_CloseController(SdlJoystickHandle ptrController) {
		sdl.joystick().SDL_CloseJoystick(ptrController);
		return true;
	}

	@Override
	protected boolean SDL_RumbleController(SdlJoystickHandle ptrController, float strong, float weak, int durationMs) {
		return sdl.joystick().SDL_RumbleJoystick(ptrController, (short) (strong * 0xFFFF), (short) (weak * 0xFFFF), durationMs);
	}

	@Override
	protected boolean SDL_RumbleControllerTriggers(SdlJoystickHandle ptrController, float left, float right, int durationMs) {
		return sdl.joystick().SDL_RumbleJoystickTriggers(ptrController, (short) (left * 0xFFFF), (short) (right * 0xFFFF), durationMs);
	}

	@Override
	protected int SDL_GetControllerPowerInfo(SdlJoystickHandle ptrController, SdlRefs.IntRef percent) {
		return sdl.joystick().SDL_GetJoystickPowerInfo(ptrController, percent);
	}

	@Override
	protected boolean SDL_SendControllerEffect(SdlJoystickHandle ptrController, ByteBuffer effect) {
		return sdl.joystick().SDL_SendJoystickEffect(ptrController, effect);
	}

	@Override
	protected boolean SDL_SetControllerLED(SdlJoystickHandle ptrController, byte red, byte green, byte blue) {
		return sdl.joystick().SDL_SetJoystickLED(ptrController, red, green, blue);
	}
}
