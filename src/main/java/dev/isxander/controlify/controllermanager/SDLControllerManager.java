package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerInfo;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.driver.SDL3NativesManager;
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
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;

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

    public SDLControllerManager() {
        Validate.isTrue(SDL3NativesManager.isLoaded(), "SDL3 natives must be loaded before creating SDLControllerManager");

        SDL_SetEventFilter(eventFilter = new EventFilter(), Pointer.NULL);
    }

    @Override
    public void tick(boolean outOfFocus) {
        super.tick(outOfFocus);

        SDL_PumpEvents();

        if (event == null) {
            CUtil.LOGGER.error("EVENT WAS NULL SOMEHOW!! RECONSTRUCTING");
            event = new SDL_Event();
        }

        while (SDL_PollEvent(event)) {
            switch (event.type) {
                // On added, `which` refers to the device index
                case SDL_EVENT_JOYSTICK_ADDED -> {
                    SDL_JoystickID jid = event.jdevice.which;
                    Validate.notNull(jid, "JID was null");

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
                    Validate.notNull(jid, "JID was null");

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
    protected Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid;

        Optional<HIDIdentifier> hid = hidInfo.hidDevice().map(HIDDevice::asIdentifier);

        boolean isGamepad = isControllerGamepad(ucid) && !DebugProperties.FORCE_JOYSTICK;

        List<Driver> drivers = new ArrayList<>();
        if (SteamDeckUtil.DECK_MODE.isGamingMode()
            && !steamDeckConsumed
            && hidInfo.type().namespace().equals(SteamDeckUtil.STEAM_DECK_NAMESPACE)
        ) {
            Optional<SteamDeckDriver> steamDeckDriver = SteamDeckDriver.create();
            if (steamDeckDriver.isPresent()) {
                drivers.add(steamDeckDriver.get());
                steamDeckConsumed = true;
            }
        }

        String uid = hidInfo.createControllerUID(
                this.getControllerCountWithMatchingHID(hid.orElse(null))
        ).orElse("unknown-uid-" + ucid);

        if (isGamepad) {
            SDL_Gamepad ptrGamepad = SDLUtil.openGamepad(jid);
            if (DebugProperties.SDL_USE_SERIAL_FOR_UID) {
                uid = useSerialForUID(SDL_GetGamepadSerial(ptrGamepad), hid).orElse(uid);
            }

            drivers.add(new SDL3GamepadDriver(ptrGamepad, jid, hidInfo.type()));
        } else {
            SDL_Joystick ptrJoystick = SDLUtil.openJoystick(jid);
            if (DebugProperties.SDL_USE_SERIAL_FOR_UID) {
                uid = useSerialForUID(SDL_GetJoystickSerial(ptrJoystick), hid).orElse(uid);
            }

            drivers.add(new SDL3JoystickDriver(ptrJoystick, jid));
        }

        String name = drivers.get(0).getDriverName();
        String guid = SDL_GetJoystickGUIDForID(jid).toString();

        ControllerInfo info = new ControllerInfo(uid, ucid, guid, name, hidInfo.type(), hidInfo.hidDevice());
        ControllerEntity controller = new ControllerEntity(info, drivers);

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
        CUtil.LOGGER.debug("Loading gamepad mappings...");

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
                    CUtil.LOGGER.warn("Successfully applied gamepad mappings but none were found for this platform. Unsupported OS?");
                } else {
                    CUtil.LOGGER.info("Successfully loaded {} gamepad mapping entries!", count);
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
            CUtil.LOGGER.info("Using SDL to identify controller type.");
            return Optional.of(new ControllerHIDService.ControllerHIDInfo(
                    Controlify.instance().controllerTypeManager().getControllerType(new HIDIdentifier(vid, pid)),
                    Optional.of(new HIDDevice.SDLHidApi(vid, pid, guidStr))
            ));
        }

        return Optional.empty();
    }

    private static Optional<String> useSerialForUID(@Nullable String serial, Optional<HIDIdentifier> hid) {
        if (serial != null && !serial.isEmpty()) {
            String uid = "";
            if (hid.isPresent()) {
                var hex = HexFormat.of();
                HIDIdentifier hidIdentifier = hid.get();
                uid = "V"
                      + hex.toHexDigits(hidIdentifier.vendorId(), 4).toUpperCase()
                      + "-P"
                      + hex.toHexDigits(hidIdentifier.productId(), 4).toUpperCase()
                      + "-";
            }
            uid += "SN" + serial.toUpperCase();
            return Optional.of(uid);
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
