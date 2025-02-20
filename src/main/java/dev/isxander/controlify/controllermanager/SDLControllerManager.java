package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.info.ControllerInfo;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.CompoundDriver;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.driver.sdl.SDL3NativesManager;
import dev.isxander.controlify.driver.sdl.SDL3GamepadDriver;
import dev.isxander.controlify.driver.sdl.SDL3JoystickDriver;
import dev.isxander.controlify.driver.sdl.SDLUtil;
import dev.isxander.controlify.driver.steamdeck.SteamDeckDriver;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.events.SDL_EventFilter;
import dev.isxander.sdl3java.api.events.events.SDL_Event;
import dev.isxander.sdl3java.api.gamepad.SDL_Gamepad;
import dev.isxander.sdl3java.api.iostream.SDL_IOStream;
import dev.isxander.sdl3java.api.joystick.SDL_Joystick;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickGUID;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.jna.size_t;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static dev.isxander.sdl3java.api.error.SdlError.*;
import static dev.isxander.sdl3java.api.events.SDL_EventType.*;
import static dev.isxander.sdl3java.api.events.SdlEvents.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepad.*;
import static dev.isxander.sdl3java.api.iostream.SdlIOStream.*;
import static dev.isxander.sdl3java.api.joystick.SdlJoystick.*;

public class SDLControllerManager extends AbstractControllerManager {

    private SDL_Event event = new SDL_Event();

    // must keep a reference to prevent GC from collecting it and the callback failing
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final EventFilter eventFilter;

    private boolean steamDeckConsumed = false;

    public SDLControllerManager(ControlifyLogger logger) {
        super(logger);
        logger.debugLog("Controller manager using SDL3");
        logger.validateIsTrue(SDL3NativesManager.isLoaded(), "SDL3 natives must be loaded before creating SDLControllerManager");

        SDL_SetEventFilter(eventFilter = new EventFilter(), Pointer.NULL);
    }

    @Override
    public void tick(boolean outOfFocus) {
        super.tick(outOfFocus);

        SDL_PumpEvents();

        if (event == null) {
            logger.error("SDL_Event has somehow been set to null. Recreating...");
            event = new SDL_Event();
        }

        while (SDL_PollEvent(event)) {
            switch (event.type) {
                // On added, `which` refers to the device index
                case SDL_EVENT_JOYSTICK_ADDED -> {
                    SDL_JoystickID jid = event.jdevice.which;
                    logger.validateIsTrue(jid != null, "event.jdevice.which was null during SDL_EVENT_JOYSTICK_ADDED event");

                    logger.debugLog("SDL event: Joystick added: {}", jid.intValue());

                    UniqueControllerID ucid = new SDLUniqueControllerID(jid);

                    Optional<ControllerEntity> controllerOpt = tryCreate(
                            ucid,
                            fetchTypeFromSDL(jid)
                                    .orElse(new ControllerHIDService.ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty()))
                    );
                    controllerOpt.ifPresent(controller -> {
                        ControllerUtils.wrapControllerError(() -> onControllerConnected(controller, true), "Connecting controller", controller);
                    });
                }

                // On removed, `which` refers to the device instance ID
                case SDL_EVENT_JOYSTICK_REMOVED -> {
                    SDL_JoystickID jid = event.jdevice.which;
                    logger.validateIsTrue(jid != null, "event.jdevice.which was null during SDL_EVENT_JOYSTICK_REMOVED event");

                    logger.debugLog("SDL event: Joystick removed: {}", jid.intValue());

                    getController(new SDLUniqueControllerID(jid))
                            .ifPresentOrElse(
                                    this::onControllerRemoved,
                                    () -> CUtil.LOGGER.warn("Controller removed but not found: {}", jid.intValue())
                            );
                }
            }
        }

        SDL_UpdateGamepads();
        SDL_UpdateJoysticks();
    }

    @Override
    public void discoverControllers() {
        logger.debugLog("Discovering controllers...");

        SDL_JoystickID[] joysticks = SDL_GetJoysticks();
        for (SDL_JoystickID jid : joysticks) {
            Optional<ControllerEntity> controllerOpt = tryCreate(
                    new SDLUniqueControllerID(jid),
                    fetchTypeFromSDL(jid)
                            .orElse(new ControllerHIDService.ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty()))
            );
            controllerOpt.ifPresent(controller -> onControllerConnected(controller, false));
        }
    }

    @Override
    protected Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo, ControlifyLogger controllerLogger) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid();
        controllerLogger.debugLog("Creating controller: {}", jid.intValue());

        boolean isGamepad = isControllerGamepad(ucid) && !DebugProperties.FORCE_JOYSTICK;
        controllerLogger.debugLog("Controller is gamepad: {}", isGamepad);

        List<Driver> drivers = new ArrayList<>();
        if (SteamDeckUtil.DECK_MODE.isGamingMode()
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
            SDL_Gamepad ptrGamepad = SDLUtil.openGamepad(jid);
            drivers.add(new SDL3GamepadDriver(ptrGamepad, jid, hidInfo.type(), controllerLogger));
        } else {
            SDL_Joystick ptrJoystick = SDLUtil.openJoystick(jid);
            drivers.add(new SDL3JoystickDriver(ptrJoystick, jid, hidInfo.type(), controllerLogger));
        }

        controllerLogger.debugLog("Drivers: {}", drivers.stream().map(driver -> driver.getClass().getSimpleName()).collect(Collectors.joining(", ")));

        CompoundDriver compoundDriver = new CompoundDriver(drivers);

        ControllerInfo info = new ControllerInfo(ucid, hidInfo.type(), hidInfo.hidDevice());
        ControllerEntity controller = new ControllerEntity(info, compoundDriver, controllerLogger);

        controllerLogger.debugLog("Unique Controller ID: {}", info.ucid());

        this.addController(ucid, controller);
        return Optional.of(controller);
    }

    @Override
    public boolean probeConnectedControllers() {
        return SDL_HasJoystick() || SDL_HasGamepad();
    }

    @Override
    public boolean isControllerGamepad(UniqueControllerID ucid) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid;
        return SDL_IsGamepad(jid);
    }

    @Override
    protected String getControllerSystemName(UniqueControllerID ucid) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid;
        return isControllerGamepad(ucid) ? SDL_GetGamepadNameForID(jid) : SDL_GetJoystickNameForID(jid);
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
            byte[] bytes = ByteStreams.toByteArray(is);

            try (Memory memory = new Memory(bytes.length)) {
                memory.write(0, bytes, 0, bytes.length);

                SDL_IOStream stream = SDL_IOFromConstMem(memory, new size_t(bytes.length));
                if (stream == null) throw new IllegalStateException("Failed to open stream");

                int count = SDL_AddGamepadMappingsFromIO(stream, true);
                if (count < 0) {
                    CUtil.LOGGER.error("Failed to load gamepad mappings: {}", SDL_GetError());
                } else if (count == 0) {
                    CUtil.LOGGER.warn("Successfully applied gamepad mappings but none were found for this OS. Unsupported OS?");
                } else {
                    CUtil.LOGGER.log("Successfully loaded {} gamepad mapping entries!", count);
                }
            }
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to load gamepad mappings", e);
        }
    }

    private static Optional<ControllerHIDService.ControllerHIDInfo> fetchTypeFromSDL(SDL_JoystickID jid) {
        int vid = SDL_GetJoystickVendorForID(jid);
        int pid = SDL_GetJoystickProductForID(jid);
        SDL_JoystickGUID guid = SDL_GetJoystickGUIDForID(jid);
        String guidStr = guid.toString();

        if (vid != 0 && pid != 0) {
            CUtil.LOGGER.log("Using SDL to identify controller type.");
            return Optional.of(new ControllerHIDService.ControllerHIDInfo(
                    Controlify.instance().controllerTypeManager().getControllerType(new HIDIdentifier(vid, pid)),
                    Optional.of(new HIDDevice.SDLHidApi(vid, pid, guidStr))
            ));
        }

        return Optional.empty();
    }

    public record SDLUniqueControllerID(@NotNull SDL_JoystickID jid) implements UniqueControllerID {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof SDLUniqueControllerID && ((SDLUniqueControllerID) obj).jid.equals(jid);
        }

        @Override
        public String toString() {
            return "SDL-" + jid.longValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid.longValue());
        }
    }

    private static class EventFilter implements SDL_EventFilter {
        @Override
        public boolean filterEvent(Pointer userdata, SDL_Event event) {
            switch (event.type) {
                case SDL_EVENT_JOYSTICK_ADDED:
                case SDL_EVENT_JOYSTICK_REMOVED:
                    return true;
                default:
                    return false;
            }
        }
    }
}
