package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.driver.SDL2NativesManager;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.utils.CUtil;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.event.SDL_EventFilter;
import io.github.libsdl4j.api.rwops.SDL_RWops;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.lang3.Validate;

import java.io.InputStream;
import java.util.Optional;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.event.SDL_EventType.*;
import static io.github.libsdl4j.api.event.SdlEvents.*;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;
import static io.github.libsdl4j.api.rwops.SdlRWops.*;

public class SDLControllerManager extends AbstractControllerManager {
    private final Controlify controlify;

    private final Int2ObjectMap<Controller<?, ?>> controllersByJid = new Int2ObjectArrayMap<>();
    private final SDL_Event event = new SDL_Event();

    // must keep a reference to prevent GC from collecting it and the callback failing
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final EventFilter eventFilter;

    public SDLControllerManager() {
        Validate.isTrue(SDL2NativesManager.isLoaded(), "SDL2 natives must be loaded before creating SDLControllerManager");

        this.controlify = Controlify.instance();

        SDL_SetEventFilter(eventFilter = new EventFilter(), Pointer.NULL);
    }

    @Override
    public void tick(boolean outOfFocus) {
        super.tick(outOfFocus);

        // SDL identifiers controllers in two different ways:
        // device index, and device instance ID.
        while (SDL_PollEvent(event) == 1) {
            switch (event.type) {
                // On added, `which` refers to the device index
                case SDL_JOYDEVICEADDED -> {
                    int deviceIndex = event.jdevice.which;
                    Optional<Controller<?, ?>> controllerOpt = createOrGet(
                            deviceIndex,
                            ControllerHIDService.fetchTypeFromSDL(deviceIndex)
                                    .orElse(new ControllerHIDService.ControllerHIDInfo(ControllerType.UNKNOWN, Optional.empty()))
                    );
                    controllerOpt.ifPresent(controller -> onControllerConnected(controller, true));
                }

                // On removed, `which` refers to the device instance ID
                case SDL_JOYDEVICEREMOVED -> {
                    int jid = event.jdevice.which;
                    getController(jid).ifPresent(this::onControllerRemoved);
                }
            }
        }
    }

    @Override
    public void discoverControllers() {
        for (int i = 0; i < SDL_NumJoysticks(); i++) {
            Optional<Controller<?, ?>> controllerOpt = createOrGet(i, controlify.controllerHIDService().fetchType(i));
            controllerOpt.ifPresent(controller -> onControllerConnected(controller, false));
        }
    }

    @Override
    public boolean probeConnectedControllers() {
        return SDL_NumJoysticks() > 0;
    }

    @Override
    public boolean isControllerGamepad(int jid) {
        return SDL_IsGameController(jid);
    }

    @Override
    protected String getControllerSystemName(int joystickId) {
        return isControllerGamepad(joystickId) ? SDL_GameControllerNameForIndex(joystickId) : SDL_JoystickNameForIndex(joystickId);
    }

    @Override
    protected void addController(int index, Controller<?, ?> controller) {
        super.addController(index, controller);

        // the instance id is technically a long, but it's usually only like 0, 1, 2, 3, etc.
        int joystickId = SDL_JoystickGetDeviceInstanceID(index).intValue();
        controllersByJid.put(joystickId, controller);
    }

    private Optional<Controller<?, ?>> getController(int joystickId) {
        return Optional.ofNullable(controllersByJid.get(joystickId));
    }

    @Override
    protected void loadGamepadMappings(Resource resource) {
        CUtil.LOGGER.debug("Loading gamepad mappings...");

        try (InputStream is = resource.open()) {
            byte[] bytes = ByteStreams.toByteArray(is);

            try (Memory memory = new Memory(bytes.length)) {
                memory.write(0, bytes, 0, bytes.length);
                SDL_RWops rw = SDL_RWFromConstMem(memory, bytes.length);
                int count = SDL_GameControllerAddMappingsFromRW(rw, 1);
                if (count < 1) {
                    CUtil.LOGGER.error("Failed to load gamepad mappings: {}", SDL_GetError());
                }
            }
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to load gamepad mappings", e);
        }
    }

    private static class EventFilter implements SDL_EventFilter {
        @Override
        public int filterEvent(Pointer userdata, SDL_Event event) {
            switch (event.type) {
                case SDL_JOYDEVICEADDED:
                case SDL_JOYDEVICEREMOVED:
                    return 1;
                default:
                    return 0;
            }
        }
    }
}
