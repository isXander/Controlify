/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.sdl;

import dev.isxander.sdl.Sdl;

public class SDLException extends RuntimeException {
	public SDLException(String message) {
		super(message);
	}

	public static SDLException useSDLError(Sdl sdl, String message) {
		return new SDLException(message + ": " + sdl.error().SDL_GetError());
	}

	public static SDLException useSDLError(Sdl sdl) {
		return useSDLError(sdl, "SDL error");
	}
}
