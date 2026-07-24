/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils;

public enum Platform {
	UNKNOWN,
	MAC,
	LINUX,
	WINDOWS,
	ANDROID,
	IOS;

	public enum Architecture {
		X64,
		X86,
		ARM64,
		ARM32,
		RISCV64,
		OTHER
	}

	private static final Platform currentPlatform;
	static {
		String osName = System.getProperty("os.name");

		if (osName.startsWith("Linux")) {
			if ("dalvik".equalsIgnoreCase(System.getProperty("java.vm.name"))) {
				currentPlatform = ANDROID;
			} else {
				currentPlatform = LINUX;
			}
		} else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
			currentPlatform = MAC;
		} else if (osName.startsWith("Windows")) {
			currentPlatform = WINDOWS;
		} else if (osName.startsWith("iOS")) {
			currentPlatform = IOS;
		} else {
			CUtil.LOGGER.log("Unable to determine platform: " + osName);
			currentPlatform = UNKNOWN;
		}
	}

	public static Platform current() {
		return currentPlatform;
	}
}
