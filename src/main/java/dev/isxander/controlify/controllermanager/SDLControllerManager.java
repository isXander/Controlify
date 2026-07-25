/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controllermanager;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.info.ControllerInfo;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.CompoundDriver;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.driver.sdl.SDL3GamepadDriver;
import dev.isxander.controlify.driver.sdl.SDL3JoystickDriver;
import dev.isxander.controlify.driver.sdl.SDLUtil;
import dev.isxander.controlify.driver.steamdeck.SteamDeckDriver;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.hid.ControllerHIDInfo;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDID;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl.*;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static dev.isxander.sdl.SdlEvents.*;

public class SDLControllerManager extends AbstractControllerManager {

	private final Sdl sdl;

	private SdlEvent event = new SdlEvent();

	// must keep a reference to prevent GC from collecting it and the callback failing
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private final EventFilter eventFilter;

	private boolean steamDeckConsumed = false;

	public SDLControllerManager(Sdl sdl, ControlifyLogger logger) {
		super(logger);
		this.sdl = sdl;
		logger.debugLog("Controller manager using SDL3");

		sdl.events().SDL_SetEventFilter(eventFilter = new EventFilter(), SdlPointer.NULL);

		this.loadGamepadMappings(minecraft.getResourceManager());
	}

	@Override
	public void tick(boolean outOfFocus) {
		if (event == null) {
			logger.warn("SDL_Event has somehow been set to null. Recreating...");
			event = new SdlEvent();
		}

		while (sdl.events().SDL_PollEvent(event)) {
			switch (event.type()) {
				// On added, `which` refers to the device index
				case SDL_EVENT_JOYSTICK_ADDED -> {
					var jdevice = (SdlEvent.JoyDevice) event.data();
					SdlJoystickId jid = jdevice.which();
					logger.validateIsTrue(jid != null, "event.jdevice.which was null during SDL_EVENT_JOYSTICK_ADDED event");

					logger.debugLog("SDL event: Joystick added: {}", jid.value());

					UniqueControllerID ucid = new SDLUniqueControllerID(jid);

					Optional<ControllerEntity> controllerOpt = tryCreate(
							ucid,
							fetchTypeFromSDL(sdl, jid)
									.orElse(new ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty()))
					);
					controllerOpt.ifPresent(controller -> {
						ControllerUtils.wrapControllerError(() -> onControllerConnected(controller, true), "Connecting controller", controller);
					});
				}

				// On removed, `which` refers to the device instance ID
				case SDL_EVENT_JOYSTICK_REMOVED -> {
					var jdevice = (SdlEvent.JoyDevice) event.data();
					SdlJoystickId jid = jdevice.which();
					logger.validateIsTrue(jid != null, "event.jdevice.which was null during SDL_EVENT_JOYSTICK_REMOVED event");

					logger.debugLog("SDL event: Joystick removed: {}", jid.value());

					getController(new SDLUniqueControllerID(jid))
							.ifPresentOrElse(
									this::onControllerRemoved,
									() -> CUtil.LOGGER.warn("Controller removed but not found: {}", jid.value())
							);
				}
			}
		}

		super.tick(outOfFocus);
	}

	@Override
	public void discoverControllers() {
		logger.debugLog("Discovering controllers...");

		SdlJoystickId[] joysticks = sdl.joystick().SDL_GetJoysticks();
		for (SdlJoystickId jid : joysticks) {
			Optional<ControllerEntity> controllerOpt = tryCreate(
					new SDLUniqueControllerID(jid),
					fetchTypeFromSDL(sdl, jid)
							.orElse(new ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty()))
			);
			controllerOpt.ifPresent(controller -> onControllerConnected(controller, false));
		}
	}

	@Override
	protected Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDInfo hidInfo, ControlifyLogger controllerLogger) {
		SdlJoystickId jid = ((SDLUniqueControllerID) ucid).jid();
		controllerLogger.debugLog("Creating controller: {}", jid.value());

		boolean isGamepad = isControllerGamepad(ucid) && !DebugProperties.FORCE_JOYSTICK;
		controllerLogger.debugLog("Controller is gamepad: {}", isGamepad);

		List<Driver> drivers = new ArrayList<>();
		if ((SteamDeckUtil.DECK_MODE.isGamingMode() || DebugProperties.STEAM_DECK_CUSTOM_CEF_URL != null)
			&& !steamDeckConsumed
			&& hidInfo.type().namespace().equals(SteamDeckUtil.STEAM_DECK_NAMESPACE)
		) {
			controllerLogger.debugLog("Controller is steam deck candidate");
			Optional<SteamDeckDriver> steamDeckDriver = SteamDeckDriver.create(controllerLogger);
			if (steamDeckDriver.isPresent()) {
				drivers.add(steamDeckDriver.get());
				steamDeckConsumed = true;
				controllerLogger.debugLog("Adding SteamDeckDriver - this controller has been reserved for Steam Deck");
			}
		}

		if (isGamepad) {
			SdlGamepadHandle ptrGamepad = SDLUtil.openGamepad(sdl, jid);
			drivers.add(new SDL3GamepadDriver(sdl, ptrGamepad, jid, hidInfo.type(), controllerLogger));
		} else {
			SdlJoystickHandle ptrJoystick = SDLUtil.openJoystick(sdl, jid);
			drivers.add(new SDL3JoystickDriver(sdl, ptrJoystick, jid, hidInfo.type(), controllerLogger));
		}

		controllerLogger.debugLog("Drivers: {}", drivers.stream().map(driver -> driver.getClass().getSimpleName()).collect(Collectors.joining(", ")));

		CompoundDriver compoundDriver = new CompoundDriver(drivers);

		ControllerInfo info = new ControllerInfo(ucid, hidInfo.type(), hidInfo.hidDevice());
		ControllerEntity controller = new ControllerEntity(
				info,
				compoundDriver,
				this.controlify.config().getActiveProfile(),
				ProfileSettings.createDefault(),
				controllerLogger
		);

		controllerLogger.debugLog("Unique Controller ID: {}", info.ucid());

		this.addController(ucid, controller);
		return Optional.of(controller);
	}

	@Override
	public boolean probeConnectedControllers() {
		return sdl.joystick().SDL_HasJoystick() || sdl.gamepad().SDL_HasGamepad();
	}

	@Override
	public boolean isControllerGamepad(UniqueControllerID ucid) {
		SdlJoystickId jid = ((SDLUniqueControllerID) ucid).jid;
		return sdl.gamepad().SDL_IsGamepad(jid);
	}

	@Override
	protected String getControllerSystemName(UniqueControllerID ucid) {
		SdlJoystickId jid = ((SDLUniqueControllerID) ucid).jid;
		return isControllerGamepad(ucid)
			? sdl.gamepad().SDL_GetGamepadNameForID(jid)
			: sdl.joystick().SDL_GetJoystickNameForID(jid);
	}

	private Optional<ControllerEntity> getController(UniqueControllerID ucid) {
		return Optional.ofNullable(controllersByJid.getOrDefault(ucid, null));
	}

	@Override
	protected void loadGamepadMappings(ResourceProvider resourceProvider) {
		CUtil.LOGGER.debugLog("Loading gamepad mappings...");

		Optional<Resource> resourceOpt = resourceProvider
				.getResource(CUtil.rl("controllers/gamecontrollerdb-sdl3.txt"));
		if (resourceOpt.isEmpty()) {
			CUtil.LOGGER.error("Failed to find game controller database.");
			return;
		}

		try (InputStream is = resourceOpt.get().open()) {
			byte[] bytes = is.readAllBytes();
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
			byteBuffer.put(bytes);
			byteBuffer.flip();

			SdlIoStreamHandle stream = sdl.ioStream().SDL_IOFromConstMem(byteBuffer);
			if (stream == null) throw new IllegalStateException("Failed to open stream");

			int count = sdl.gamepad().SDL_AddGamepadMappingsFromIO(stream, true);
			if (count < 0) {
				CUtil.LOGGER.error("Failed to load gamepad mappings: {}", sdl.error().SDL_GetError());
			} else if (count == 0) {
				CUtil.LOGGER.warn("Successfully applied gamepad mappings but none were found for this OS. Unsupported OS?");
			} else {
				CUtil.LOGGER.log("Successfully loaded {} gamepad mapping entries!", count);
			}
		} catch (Throwable e) {
			CUtil.LOGGER.error("Failed to load gamepad mappings", e);
		}
	}

	private static Optional<ControllerHIDInfo> fetchTypeFromSDL(Sdl sdl, SdlJoystickId jid) {
		int vid = sdl.joystick().SDL_GetJoystickVendorForID(jid);
		int pid = sdl.joystick().SDL_GetJoystickProductForID(jid);
		SdlGuid guid = sdl.joystick().SDL_GetJoystickGUIDForID(jid);
		String guidStr = guid.toString();

		if (vid != 0 && pid != 0) {
			CUtil.LOGGER.log("Using SDL to identify controller type.");
			return Optional.of(new ControllerHIDInfo(
					Controlify.instance().controllerTypeManager().getControllerType(new HIDID(vid, pid)),
					Optional.of(new HIDDevice(new HIDID(vid, pid), guidStr))
			));
		}

		return Optional.empty();
	}

	public record SDLUniqueControllerID(@NotNull SdlJoystickId jid) implements UniqueControllerID {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof SDLUniqueControllerID && ((SDLUniqueControllerID) obj).jid.equals(jid);
		}

		@Override
		public String toString() {
			return "SDL-" + jid.value();
		}

		@Override
		public int hashCode() {
			return Objects.hash(jid.value());
		}
	}

	private static class EventFilter implements SdlCallbacks.EventFilter {
		@Override
		public boolean filter(SdlPointer userdata, SdlEvent event) {
			return switch (event.type()) {
				case SDL_EVENT_JOYSTICK_ADDED,
					SDL_EVENT_JOYSTICK_REMOVED -> true;
				default -> false;
			};
		}
	}
}
