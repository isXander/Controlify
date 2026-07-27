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
		X64(true),
		X86(false),
		ARM64(true),
		ARM32(false),
		RISCV64(true),
		UNKNOWN(true);

		private final boolean is64Bit;

		Architecture(boolean is64Bit) {
			this.is64Bit = is64Bit;
		}

		public boolean is64Bit() {
			return is64Bit;
		}

		private static final Architecture current;
		static {
			String osArch = System.getProperty("os.arch");
			boolean is64Bit = osArch.contains("64") || osArch.startsWith("armv8");

			if (osArch.startsWith("arm") || osArch.startsWith("aarch")) {
				current = is64Bit ? ARM64 : ARM32;
			} else if (osArch.startsWith("ppc")) {
				current = UNKNOWN;
			} else if (osArch.startsWith("riscv")) {
				if ("riscv64".equals(osArch)) {
					current = RISCV64;
				} else {
					current = UNKNOWN;
				}
			} else {
				current = is64Bit ? X64 : X86;
			}
		}

		public static Architecture current() {
			return current;
		}
	}

	private static final Platform current;
	private static final String resourcePrefix;

	static {
		String osName = System.getProperty("os.name");

		if (osName.startsWith("Linux")) {
			if ("dalvik".equalsIgnoreCase(System.getProperty("java.vm.name")) || System.getenv("POJAV_NATIVEDIR") != null) {
				current = ANDROID;
			} else {
				current = LINUX;
			}
		} else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
			current = MAC;
		} else if (osName.startsWith("Windows")) {
			current = WINDOWS;
		} else if (osName.startsWith("iOS")) {
			current = IOS;
		} else {
			CUtil.LOGGER.log("Unable to determine platform: " + osName);
			current = UNKNOWN;
		}

		resourcePrefix = getResourcePrefix(current(), Architecture.current());
	}

	public static Platform current() {
		return current;
	}

	public static String getResourcePrefix() {
		return resourcePrefix;
	}

	private static String getResourcePrefix(Platform platform, Architecture arch) {
		String platformPrefix = switch (platform) {
			case WINDOWS -> "win32";
			case LINUX -> "linux";
			case MAC -> "darwin";
			case ANDROID -> "android";
			case IOS -> "ios";
			case UNKNOWN -> "unknown";
		};

		String archSuffix = switch (arch) {
			case X64 -> "x86-64";
			case X86 -> "x86";
			case ARM64 -> "aarch64";
			case ARM32 -> "aarch32";
			case RISCV64 -> "riscv64";
			case UNKNOWN -> "unknown";
		};

		return platformPrefix + "-" + archSuffix;
	}
}
