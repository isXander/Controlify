/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils;

import dev.isxander.controlify.debug.DebugProperties;

public class DebugLog {
	public static void log(String message, Object... args) {
		if (DebugProperties.DEBUG_LOGGING) {
			CUtil.LOGGER.debugLog(message, args);
		}
	}
}
