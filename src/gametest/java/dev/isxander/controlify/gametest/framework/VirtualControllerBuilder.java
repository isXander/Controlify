/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework;

import dev.isxander.controlify.driver.sdl.SDLException;
import dev.isxander.controlify.test.framework.SdlVirtualJoystickDescBuilder;
import dev.isxander.sdl.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class VirtualControllerBuilder {
	private final ClientGameTestContext context;
	private final ControlifyGameTestContext controlify;
	private final Sdl sdl;

	private final SdlVirtualJoystickDescBuilder descBuilder;
	private ControllerInputDefinition inputs = new ControllerInputDefinition(Set.of(), Set.of());
	private @Nullable String gamepadMapping = null;

	public VirtualControllerBuilder(ClientGameTestContext context, ControlifyGameTestContext controlify, Sdl sdl) {
		this.context = context;
		this.controlify = controlify;
		this.sdl = sdl;
		this.descBuilder = new SdlVirtualJoystickDescBuilder();
	}

	public VirtualControllerBuilder withXbox() {
		this.withInputs(ControllerInputDefinition.XINPUT);
		this.descBuilder
			.withName("Virtual Xbox Controller")
			.withVendorId(0x45e)
			.withProductId(0x2ff);
		return this;
	}

	public VirtualControllerBuilder withDualsense() {
		this.withInputs(ControllerInputDefinition.ALL_SDL_GAMEPAD_INPUTS);
		this.descBuilder
			.withName("Virtual Dualsense Controller")
			.withVendorId(0x54c)
			.withProductId(0xce6);
		return this;
	}

	public VirtualControllerBuilder withGyro() {
		this.descBuilder
			.withSensor(SdlGamepad.SDL_SENSOR_GYRO, 2000);
		return this;
	}

	public VirtualControllerBuilder withTouchpad(int numFingers) {
		this.descBuilder
			.withTouchpad(numFingers);
		return this;
	}

	public VirtualControllerBuilder withInputs(ControllerInputDefinition inputs) {
		this.inputs = inputs;
		this.descBuilder
			.withGamepadType()
			.withNumButtons(inputs.numButtons())
			.withNumAxes(inputs.numAxes())
			.withButtonMask(inputs.buttonMask())
			.withAxisMask(inputs.axisMask());
		return this;
	}

	public VirtualControllerBuilder withGamepadMapping(String mapping) {
		this.gamepadMapping = mapping;
		return this;
	}

	public TestControllerContext<ByteBuffer> attach() {
		return this.attachWithDriver(new GamepadEffectDriver.Noop());
	}

	public <E> TestControllerContext<E> attachWithDriver(@Nullable GamepadEffectDriver<E> driver) {
		var state = new TestControllerState<>(driver);

		SdlVirtualJoystickDesc virtualJoystickDesc = this.descBuilder
			.withSetPlayerIndex((_, playerIndex) -> state.setPlayerIndex(playerIndex))
			.withRumble((_, low, high) -> {
				state.setRumble(low, high);
				return true;
			})
			.withRumbleTriggers((_, left, right) -> {
				state.setTriggerRumble(left, right);
				return true;
			})
			.withSetLed((_, red, green, blue) -> {
				state.setLed(red, green, blue);
				return true;
			})
			.withSendEffect((_, effect) -> {
				// copy to a heap-allocated buffer
				var buffer = ByteBuffer.allocate(effect.remaining());
				buffer.put(effect).flip().asReadOnlyBuffer();

				state.setLatestEffect(buffer);
				return true;
			})
			.withSetSensorsEnabled((_, enabled) -> {
				state.setSensorsEnabled(enabled);
				return true;
			})
			.build();

		SdlJoystickId jid = this.sdl.joystick().SDL_AttachVirtualJoystick(virtualJoystickDesc);
		if (jid.value() == 0) {
			throw SDLException.useSDLError(sdl, "Failed to attach virtual joystick");
		}

		if (this.gamepadMapping != null) {
			this.sdl.gamepad().SDL_SetGamepadMapping(jid, this.gamepadMapping);
		}

		SdlJoystickHandle handle = this.sdl.joystick().SDL_OpenJoystick(jid);
		if (handle.isNull()) {
			throw SDLException.useSDLError(sdl, "Failed to open virtual joystick");
		}

		// Allow Controlify to poll and add controller
		this.context.waitTick();

		return new TestControllerContext<>(this.context, this.sdl, handle, jid, this.inputs, state);
	}
}
