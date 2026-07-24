/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Platform;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl.Sdl;
import dev.isxander.sdl.SdlLoader;
import dev.isxander.sdl.SdlVersion;
import org.apache.commons.io.function.IOSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.ServiceLoader;

import static dev.isxander.sdl.SdlHints.*;
import static dev.isxander.sdl.SdlInit.*;

public class SDLNativesLoader {
	private static final boolean LWJGL_SDL_AVAILABLE = /*? if >=26.3 {*/ /*true *//*?} else {*/ false /*?}*/;
	private static final boolean NATIVES_IN_JAR = /*? if natives_in_jar {*/ true /*?} else {*/ /*false *//*?}*/;

	private static final String NATIVE_SDL_NAME = System.mapLibraryName("SDL3");
	private static final String NATIVE_SDL_PATH = Platform.RESOURCE_PREFIX + "/" + NATIVE_SDL_NAME;

	private static final ControlifyLogger LOGGER = CUtil.LOGGER.createSubLogger("SDLNativesLoader");

	public static Sdl load() {
		ServiceLoader<SdlLoader> serviceLoader = ServiceLoader.load(SdlLoader.class);

		Sdl sdl = loadFromControlifyNatives(serviceLoader)
			.or(() -> loadFromLwjgl(serviceLoader))
			.or(() -> loadFromNativesInJar(serviceLoader))
			.orElseThrow(() -> new IllegalStateException("Could not load SDL natives"));

		startSdl(sdl);

		return sdl;
	}

	private static Optional<Sdl> loadFromControlifyNatives(ServiceLoader<SdlLoader> serviceLoader) {
		SdlLoader sdlLoader = serviceLoader.stream()
			.map(ServiceLoader.Provider::get)
			.filter(loader -> loader.name().equals("ffm"))
			.findAny().orElse(null);

		if (sdlLoader == null) {
			return Optional.empty();
		}

		Optional<Sdl> sdl = PlatformMainUtil.getModFileInputStream("controlify_natives", NATIVE_SDL_PATH)
			.flatMap(path -> loadFromInputStream(sdlLoader, path));

		sdl.ifPresent(_ -> LOGGER.log("Loaded SDL from controlify_natives"));

		return sdl;
	}

	private static Optional<Sdl> loadFromLwjgl(ServiceLoader<SdlLoader> serviceLoader) {
		if (!LWJGL_SDL_AVAILABLE) {
			return Optional.empty();
		}

		return Optional.empty();
	}

	private static Optional<Sdl> loadFromNativesInJar(ServiceLoader<SdlLoader> serviceLoader) {
		if (!NATIVES_IN_JAR) {
			return Optional.empty();
		}

		SdlLoader sdlLoader = serviceLoader.stream()
			.map(ServiceLoader.Provider::get)
			.filter(loader -> loader.name().equals("ffm"))
			.findAny().orElse(null);

		if (sdlLoader == null) {
			return Optional.empty();
		}

		return PlatformMainUtil.getModFileInputStream("controlify", NATIVE_SDL_PATH)
			.flatMap(path -> loadFromInputStream(sdlLoader, path));
	}

	private static Optional<Sdl> loadFromInputStream(SdlLoader sdlLoader, IOSupplier<InputStream> supplier) {
		Path temporaryFile = null;
		try(InputStream is = supplier.get()) {
			temporaryFile = Files.createTempFile("controlify-native-", NATIVE_SDL_NAME);

			Files.copy(
				is,
				temporaryFile,
				StandardCopyOption.REPLACE_EXISTING
			);
		} catch (IOException e) {
			LOGGER.error("Failed to copy from input stream -> {}", e, temporaryFile);
			return Optional.empty();
		}

		temporaryFile.toFile().deleteOnExit();

		try {
			return Optional.of(sdlLoader.create(temporaryFile));
		} catch (Exception e) {
			LOGGER.error("Failed to bind SDL from input stream", e);
			return Optional.empty();
		}
	}

	private static void startSdl(Sdl sdl) {
		sdl.hints().SDL_SetHint(SDL_HINT_WINDOWS_GAMEINPUT, "1");
		sdl.hints().SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI, "1");
		sdl.hints().SDL_SetHint(SDL_HINT_JOYSTICK_ENHANCED_REPORTS, "1");
		sdl.hints().SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI_STEAM, "1");
		sdl.hints().SDL_SetHint(SDL_HINT_JOYSTICK_ROG_CHAKRAM, "1");
		sdl.hints().SDL_SetHint(SDL_HINT_JOYSTICK_ALLOW_BACKGROUND_EVENTS, "1");
		sdl.hints().SDL_SetHint(SDL_HINT_JOYSTICK_LINUX_DEADZONES, "1");

		SdlVersion.SdlVersionNumber nativesVersion = SdlVersion.SdlVersionNumber.fromPacked(sdl.version().SDL_GetVersion());
		SdlVersion.SdlVersionNumber javaVersion = sdl.version().SDL_GetJavaBindingsVersion();
		LOGGER.log("Loading SDL3 version: {}. Java bindings targeting: {}", nativesVersion, javaVersion);
		if (!nativesVersion.equals(javaVersion)) {
			LOGGER.warn("SDL3 NATIVE LIBRARY VERSION MISMATCH! Java bindings are targeting a different version of SDL3 than the loaded native library. This may cause issues.");
		}

		// initialise SDL with just joystick and gamecontroller subsystems
		if (!sdl.init().SDL_Init(SDL_INIT_JOYSTICK | SDL_INIT_GAMEPAD | SDL_INIT_EVENTS)) {
			LOGGER.error("Failed to initialise SDL3: {}", sdl.error().SDL_GetError());
			throw new RuntimeException("Failed to initialise SDL3: " + sdl.error().SDL_GetError());
		}

		if (!sdl.init().SDL_InitSubSystem(SDL_INIT_AUDIO)) {
			LOGGER.warn("Failed to initialise SDL3's audio subsystem, continuing without audio: {}", sdl.error().SDL_GetError());
		}

		LOGGER.log("Successfully initialised SDL subsystems");
	}
}
