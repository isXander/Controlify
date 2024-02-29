package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.SDL3NativesManager;
import dev.isxander.controlify.driver.sdl.SDL3GamepadDriver;
import dev.isxander.controlify.driver.sdl.SDL3JoystickDriver;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ControllerUtils;
import io.github.libsdl4j.api.events.SDL_EventFilter;
import io.github.libsdl4j.api.events.events.SDL_Event;
import io.github.libsdl4j.api.joystick.SDL_JoystickGUID;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;
import io.github.libsdl4j.api.rwops.SDL_RWops;
import io.github.libsdl4j.jna.size_t;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.lang3.Validate;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.events.SDL_EventType.*;
import static io.github.libsdl4j.api.events.SdlEvents.*;
import static io.github.libsdl4j.api.gamepad.SdlGamepad.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;
import static io.github.libsdl4j.api.rwops.SdlRWops.*;

public class SDLControllerManager extends AbstractControllerManager {

    private final SDL_Event event = new SDL_Event();

    // must keep a reference to prevent GC from collecting it and the callback failing
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final EventFilter eventFilter;

    public SDLControllerManager() {
        Validate.isTrue(SDL3NativesManager.isLoaded(), "SDL3 natives must be loaded before creating SDLControllerManager");

        SDL_SetEventFilter(eventFilter = new EventFilter(), Pointer.NULL);
    }

    @Override
    public void tick(boolean outOfFocus) {
        super.tick(outOfFocus);

        // SDL identifiers controllers in two different ways:
        // device index, and device instance ID.
        while (SDL_PollEvent(event)) {
            System.out.println(event.type);
            switch (event.type) {
                // On added, `which` refers to the device index
                case SDL_EVENT_JOYSTICK_ADDED -> {
                    SDL_JoystickID jid = event.jdevice.which;

                    UniqueControllerID ucid = new SDLUniqueControllerID(jid);

                    Optional<ControllerEntity> controllerOpt = createOrGet(
                            ucid,
                            fetchTypeFromSDL(jid)
                                    .orElse(new ControllerHIDService.ControllerHIDInfo(ControllerType.UNKNOWN, Optional.empty()))
                    );
                    controllerOpt.ifPresent(controller -> {
                        ControllerUtils.wrapControllerError(() -> onControllerConnected(controller, true), "Connecting controller", controller);
                    });
                }

                // On removed, `which` refers to the device instance ID
                case SDL_EVENT_JOYSTICK_REMOVED -> {
                    SDL_JoystickID jid = event.jdevice.which;
                    CUtil.LOGGER.info("Controller removed: {}", jid.intValue());
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
            Optional<ControllerEntity> controllerOpt = createOrGet(
                    new SDLUniqueControllerID(jid),
                    fetchTypeFromSDL(jid)
                            .orElse(new ControllerHIDService.ControllerHIDInfo(ControllerType.UNKNOWN, Optional.empty()))
            );
            controllerOpt.ifPresent(controller -> onControllerConnected(controller, false));
        }
    }

    @Override
    protected Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid;

        Optional<HIDIdentifier> hid = hidInfo.hidDevice().map(HIDDevice::asIdentifier);
        String uid = hidInfo.createControllerUID().orElse("unknown-uid-" + jid.intValue());
        boolean isGamepad = isControllerGamepad(ucid) && !DebugProperties.FORCE_JOYSTICK;
        if (isGamepad) {
            SDL3GamepadDriver driver = new SDL3GamepadDriver(jid, hidInfo.type(), uid, ucid, hid);
            System.out.println(driver.getUcid());
            this.addController(driver.getUcid(), driver.getController(), driver);

            return Optional.of(driver.getController());
        } else {
            SDL3JoystickDriver driver = new SDL3JoystickDriver(jid, hidInfo.type(), uid, ucid, hid);
            this.addController(driver.getUcid(), driver.getController(), driver);

            return Optional.of(driver.getController());
        }
    }

    @Override
    public boolean probeConnectedControllers() {
        return SDL_GetJoysticks().length > 0;
    }

    @Override
    public boolean isControllerGamepad(UniqueControllerID ucid) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid;
        return SDL_IsGamepad(jid);
    }

    @Override
    protected String getControllerSystemName(UniqueControllerID ucid) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid;
        return isControllerGamepad(ucid) ? SDL_GetGamepadInstanceName(jid) : SDL_GetJoystickInstanceName(jid);
    }

    private Optional<ControllerEntity> getController(UniqueControllerID ucid) {
        return Optional.ofNullable(controllersByJid.getOrDefault(ucid, null));
    }

    @Override
    protected void loadGamepadMappings(Resource resource) {
        CUtil.LOGGER.debug("Loading gamepad mappings...");

        try (InputStream is = resource.open()) {
            byte[] bytes = ByteStreams.toByteArray(is);

            try (Memory memory = new Memory(bytes.length)) {
                memory.write(0, bytes, 0, bytes.length);
                SDL_RWops rw = SDL_RWFromConstMem(memory, new size_t(bytes.length));
                int count = SDL_AddGamepadMappingsFromRW(rw, true);
                if (count < 1) {
                    CUtil.LOGGER.error("Failed to load gamepad mappings: {}", SDL_GetError());
                }
            }
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to load gamepad mappings", e);
        }
    }

    private static Optional<ControllerHIDService.ControllerHIDInfo> fetchTypeFromSDL(SDL_JoystickID jid) {
        int vid = SDL_GetJoystickInstanceVendor(jid);
        int pid = SDL_GetJoystickInstanceProduct(jid);
        SDL_JoystickGUID guid = SDL_GetJoystickInstanceGUID(jid);
        String guidStr = guid.toString();

        if (vid != 0 && pid != 0) {
            CUtil.LOGGER.info("Using SDL to identify controller type.");
            return Optional.of(new ControllerHIDService.ControllerHIDInfo(
                    ControllerType.getTypeForHID(new HIDIdentifier(vid, pid)),
                    Optional.of(new HIDDevice.SDLHidApi(vid, pid, guidStr))
            ));
        }

        return Optional.empty();
    }

    public record SDLUniqueControllerID(SDL_JoystickID jid) implements UniqueControllerID {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof SDLUniqueControllerID && ((SDLUniqueControllerID) obj).jid.equals(jid);
        }

        @Override
        public String toString() {
            return "SDL-" + jid.intValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid.intValue());
        }
    }

    private static class EventFilter implements SDL_EventFilter {
        @Override
        public int filterEvent(Pointer userdata, SDL_Event event) {
            switch (event.type) {
                case SDL_EVENT_JOYSTICK_ADDED:
                case SDL_EVENT_JOYSTICK_REMOVED:
                    return 1;
                default:
                    return 0;
            }
        }
    }
}
