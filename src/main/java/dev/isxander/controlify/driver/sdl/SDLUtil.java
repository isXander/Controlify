/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.sdl;

import dev.isxander.sdl.Sdl;
import dev.isxander.sdl.SdlGamepadHandle;
import dev.isxander.sdl.SdlJoystickHandle;
import dev.isxander.sdl.SdlJoystickId;

public class SDLUtil {
	public static SdlGamepadHandle openGamepad(Sdl sdl, SdlJoystickId jid) {
		SdlGamepadHandle gamepad = sdl.gamepad().SDL_OpenGamepad(jid);
		if (gamepad == null) {
			throw SDLException.useSDLError(sdl, "Failed to open gamepad");
		}
		return gamepad;
	}

	public static SdlJoystickHandle openJoystick(Sdl sdl, SdlJoystickId jid) {
		SdlJoystickHandle joystick = sdl.joystick().SDL_OpenJoystick(jid);
		if (joystick == null) {
			throw SDLException.useSDLError(sdl, "Failed to open joystick");
		}
		return joystick;
	}
}
