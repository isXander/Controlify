/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework.controller;

import java.util.Set;

import static dev.isxander.sdl.SdlGamepad.*;

public record ControllerInputDefinition(
	Set<Integer> buttons,
	Set<Integer> axes
) {
	public int numButtons() {
		return this.buttons.size();
	}

	public int numAxes() {
		return this.axes.size();
	}

	/**
	 * SDL packs the canonical buttons selected by {@code buttonMask} into dense
	 * joystick button indices. Convert a gamepad button enum to that raw index.
	 */
	public int joystickButton(int gamepadButton) {
		if (!this.buttons.contains(gamepadButton)) {
			throw new IllegalArgumentException("Gamepad button is not available: " + gamepadButton);
		}

		return (int) this.buttons.stream()
			.filter(button -> button < gamepadButton)
			.count();
	}

	/**
	 * SDL packs the canonical axes selected by {@code axisMask} into dense
	 * joystick axis indices. Convert a gamepad axis enum to that raw index.
	 */
	public int joystickAxis(int gamepadAxis) {
		if (!this.axes.contains(gamepadAxis)) {
			throw new IllegalArgumentException("Gamepad axis is not available: " + gamepadAxis);
		}

		return (int) this.axes.stream()
			.filter(axis -> axis < gamepadAxis)
			.count();
	}

	public int buttonMask() {
		int mask = 0;

		for (int button : this.buttons) {
			mask |= 1 << button;
		}

		return mask;
	}

	public int axisMask() {
		int mask = 0;

		for (int axis : this.axes) {
			mask |= 1 << axis;
		}

		return mask;
	}

	public static final ControllerInputDefinition XINPUT = new ControllerInputDefinition(
		Set.of(
			SDL_GAMEPAD_BUTTON_SOUTH,
			SDL_GAMEPAD_BUTTON_EAST,
			SDL_GAMEPAD_BUTTON_WEST,
			SDL_GAMEPAD_BUTTON_NORTH,

			SDL_GAMEPAD_BUTTON_BACK,
			SDL_GAMEPAD_BUTTON_START,

			SDL_GAMEPAD_BUTTON_LEFT_STICK,
			SDL_GAMEPAD_BUTTON_RIGHT_STICK,

			SDL_GAMEPAD_BUTTON_LEFT_SHOULDER,
			SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER,

			SDL_GAMEPAD_BUTTON_DPAD_UP,
			SDL_GAMEPAD_BUTTON_DPAD_DOWN,
			SDL_GAMEPAD_BUTTON_DPAD_LEFT,
			SDL_GAMEPAD_BUTTON_DPAD_RIGHT
		),
		Set.of(
			SDL_GAMEPAD_AXIS_LEFTX,
			SDL_GAMEPAD_AXIS_LEFTY,
			SDL_GAMEPAD_AXIS_RIGHTX,
			SDL_GAMEPAD_AXIS_RIGHTY,
			SDL_GAMEPAD_AXIS_LEFT_TRIGGER,
			SDL_GAMEPAD_AXIS_RIGHT_TRIGGER
		)
	);

	public static final ControllerInputDefinition ALL_SDL_GAMEPAD_INPUTS = new ControllerInputDefinition(
		Set.of(
			SDL_GAMEPAD_BUTTON_SOUTH,
			SDL_GAMEPAD_BUTTON_EAST,
			SDL_GAMEPAD_BUTTON_WEST,
			SDL_GAMEPAD_BUTTON_NORTH,

			SDL_GAMEPAD_BUTTON_BACK,
			SDL_GAMEPAD_BUTTON_GUIDE,
			SDL_GAMEPAD_BUTTON_START,

			SDL_GAMEPAD_BUTTON_LEFT_STICK,
			SDL_GAMEPAD_BUTTON_RIGHT_STICK,

			SDL_GAMEPAD_BUTTON_LEFT_SHOULDER,
			SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER,

			SDL_GAMEPAD_BUTTON_DPAD_UP,
			SDL_GAMEPAD_BUTTON_DPAD_DOWN,
			SDL_GAMEPAD_BUTTON_DPAD_LEFT,
			SDL_GAMEPAD_BUTTON_DPAD_RIGHT,

			SDL_GAMEPAD_BUTTON_MISC1,

			SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1,
			SDL_GAMEPAD_BUTTON_LEFT_PADDLE1,
			SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2,
			SDL_GAMEPAD_BUTTON_LEFT_PADDLE2,

			SDL_GAMEPAD_BUTTON_TOUCHPAD,

			SDL_GAMEPAD_BUTTON_MISC2,
			SDL_GAMEPAD_BUTTON_MISC3,
			SDL_GAMEPAD_BUTTON_MISC4,
			SDL_GAMEPAD_BUTTON_MISC5,
			SDL_GAMEPAD_BUTTON_MISC6
		),
		Set.of(
			SDL_GAMEPAD_AXIS_LEFTX,
			SDL_GAMEPAD_AXIS_LEFTY,
			SDL_GAMEPAD_AXIS_RIGHTX,
			SDL_GAMEPAD_AXIS_RIGHTY,
			SDL_GAMEPAD_AXIS_LEFT_TRIGGER,
			SDL_GAMEPAD_AXIS_RIGHT_TRIGGER
		)
	);
}
