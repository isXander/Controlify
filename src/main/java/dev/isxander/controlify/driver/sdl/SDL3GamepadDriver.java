/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.touchpad.Touchpads;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl.*;
import org.joml.Vector2f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static dev.isxander.controlify.utils.CUtil.*;
import static dev.isxander.sdl.SdlGamepad.*;

public class SDL3GamepadDriver extends SDLCommonDriver<SdlGamepadHandle> {

	private static final int GYRO_AXIS_COUNT = 3;
	private static final List<SDLGamepadInputMapping> INPUT_MAPPINGS = List.of(
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_STICK_AXIS_RIGHT, SDL_GAMEPAD_AXIS_LEFTX, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_STICK_AXIS_LEFT, SDL_GAMEPAD_AXIS_LEFTX, false),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_STICK_AXIS_DOWN, SDL_GAMEPAD_AXIS_LEFTY, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_STICK_AXIS_UP, SDL_GAMEPAD_AXIS_LEFTY, false),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, SDL_GAMEPAD_AXIS_RIGHTX, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_STICK_AXIS_LEFT, SDL_GAMEPAD_AXIS_RIGHTX, false),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_STICK_AXIS_DOWN, SDL_GAMEPAD_AXIS_RIGHTY, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_STICK_AXIS_UP, SDL_GAMEPAD_AXIS_RIGHTY, false),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.LEFT_TRIGGER_AXIS, SDL_GAMEPAD_AXIS_LEFT_TRIGGER, true),
		new SDLGamepadInputMapping.AxisMapping(GamepadInputs.RIGHT_TRIGGER_AXIS, SDL_GAMEPAD_AXIS_RIGHT_TRIGGER, true),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.SOUTH_BUTTON, SDL_GAMEPAD_BUTTON_SOUTH),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.EAST_BUTTON, SDL_GAMEPAD_BUTTON_EAST),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.WEST_BUTTON, SDL_GAMEPAD_BUTTON_WEST),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.NORTH_BUTTON, SDL_GAMEPAD_BUTTON_NORTH),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.LEFT_SHOULDER_BUTTON, SDL_GAMEPAD_BUTTON_LEFT_SHOULDER),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.RIGHT_SHOULDER_BUTTON, SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.BACK_BUTTON, SDL_GAMEPAD_BUTTON_BACK),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.START_BUTTON, SDL_GAMEPAD_BUTTON_START),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.GUIDE_BUTTON, SDL_GAMEPAD_BUTTON_GUIDE),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.DPAD_UP_BUTTON, SDL_GAMEPAD_BUTTON_DPAD_UP),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.DPAD_DOWN_BUTTON, SDL_GAMEPAD_BUTTON_DPAD_DOWN),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.DPAD_LEFT_BUTTON, SDL_GAMEPAD_BUTTON_DPAD_LEFT),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.DPAD_RIGHT_BUTTON, SDL_GAMEPAD_BUTTON_DPAD_RIGHT),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.LEFT_STICK_BUTTON, SDL_GAMEPAD_BUTTON_LEFT_STICK),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.RIGHT_STICK_BUTTON, SDL_GAMEPAD_BUTTON_RIGHT_STICK),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_1_BUTTON, SDL_GAMEPAD_BUTTON_MISC1),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_2_BUTTON, SDL_GAMEPAD_BUTTON_MISC2),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_3_BUTTON, SDL_GAMEPAD_BUTTON_MISC3),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_4_BUTTON, SDL_GAMEPAD_BUTTON_MISC4),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_5_BUTTON, SDL_GAMEPAD_BUTTON_MISC5),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.MISC_6_BUTTON, SDL_GAMEPAD_BUTTON_MISC6),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.LEFT_PADDLE_1_BUTTON, SDL_GAMEPAD_BUTTON_LEFT_PADDLE1),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.LEFT_PADDLE_2_BUTTON, SDL_GAMEPAD_BUTTON_LEFT_PADDLE2),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.RIGHT_PADDLE_1_BUTTON, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.RIGHT_PADDLE_2_BUTTON, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2),
		new SDLGamepadInputMapping.ButtonMapping(GamepadInputs.TOUCHPAD_1_BUTTON, SDL_GAMEPAD_BUTTON_TOUCHPAD)
	);

	private InputComponent inputComponent;
	private GyroComponent gyroComponent;
	private TouchpadComponent touchpadComponent;

	private final boolean isGryoSupported;

	private final int numTouchpads;

	private final MemorySegment gyroMemory;
	private final FloatBuffer gyroBuffer;

	public SDL3GamepadDriver(Sdl sdl, SdlGamepadHandle ptrController, SdlJoystickId jid, ControllerType type, ControlifyLogger logger) {
		super(sdl, ptrController, jid, type, logger);

		this.isGryoSupported = sdl.gamepad().SDL_GamepadHasSensor(ptrController, SDL_SENSOR_GYRO);
		this.numTouchpads = sdl.gamepad().SDL_GetNumGamepadTouchpads(ptrController);

		if (this.isGryoSupported) {
			sdl.gamepad().SDL_SetGamepadSensorEnabled(ptrController, SDL_SENSOR_GYRO, true);

			this.gyroMemory = this.arena.allocate(ValueLayout.JAVA_FLOAT, GYRO_AXIS_COUNT);
		} else {
			this.gyroMemory = MemorySegment.NULL;
		}

		this.gyroBuffer = this.gyroMemory.asByteBuffer()
			.order(ByteOrder.nativeOrder())
			.asFloatBuffer();
	}

	@Override
	public void addComponents(ControllerEntity controller) {
		super.addComponents(controller);

		int buttonCount = (int) INPUT_MAPPINGS.stream()
				.filter(m -> m instanceof SDLGamepadInputMapping.ButtonMapping)
				.filter(m -> m.hasInput(sdl.gamepad(), ptrController))
				.count();
		int axisCount = (int) INPUT_MAPPINGS.stream()
				.filter(m -> m instanceof SDLGamepadInputMapping.AxisMapping)
				.filter(m -> m.hasInput(sdl.gamepad(), ptrController))
				.count();

		controller.setComponent(
				this.inputComponent = new InputComponent(
						controller,
						buttonCount,
						axisCount,
						0,
						true,
						GamepadInputs.DEADZONE_GROUPS,
						controller.info().type().mappingId()
				)
		);

		if (this.isGryoSupported) {
			controller.setComponent(this.gyroComponent = new GyroComponent());
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
	public void update(ControllerEntity controller, boolean outOfFocus) {
		super.update(controller, outOfFocus);

		this.updateInput();
		this.updateGyro();
		this.updateTouchpad();
	}

	private void updateInput() {
		ControllerStateImpl state = new ControllerStateImpl();

		for (SDLGamepadInputMapping m : INPUT_MAPPINGS) {
			m.applyState(sdl.gamepad(), ptrController, state);
		}

		this.inputComponent.pushState(state);
	}

	private void updateGyro() {
		if (!isGryoSupported) return;

		if (sdl.gamepad().SDL_GetGamepadSensorData(ptrController, SDL_SENSOR_GYRO, gyroBuffer)) {
			this.gyroComponent.setState(
				new GyroState(gyroBuffer.get(0), gyroBuffer.get(1), gyroBuffer.get(2))
			);
		} else {
			CUtil.LOGGER.error("Could not get gyro data: {}", sdl.error().SDL_GetError());
		}
	}

	private void updateTouchpad() {
		if (numTouchpads < 1) return;

		for (int touchpadIdx = 0; touchpadIdx < numTouchpads; touchpadIdx++) {
			Touchpads.Touchpad touchpad = this.touchpadComponent.touchpads()[touchpadIdx];

			List<Touchpads.Finger> fingers = new ArrayList<>();
			for (int fingerIdx = 0; fingerIdx < touchpad.maxFingers(); fingerIdx++) {
				var fingerState = new SdlRefs.ByteRef();
				var x = new SdlRefs.FloatRef();
				var y = new SdlRefs.FloatRef();
				var pressure = new SdlRefs.FloatRef();

				if (!sdl.gamepad().SDL_GetGamepadTouchpadFinger(ptrController, touchpadIdx, fingerIdx, fingerState, x, y, pressure)) {
					CUtil.LOGGER.error("Failed to fetch touchpad finger: {}", sdl.error().SDL_GetError());
				} else if (fingerState.value == 1) {
					fingers.add(
							new Touchpads.Finger(
									fingerIdx,
									// SDL already returns the correct range for touchpad position and pressure
									new Vector2f(x.value, y.value),
									pressure.value
							)
					);
				}
			}

			touchpad.pushFingers(fingers);
		}
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
