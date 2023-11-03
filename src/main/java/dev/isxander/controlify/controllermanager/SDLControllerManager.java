package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.driver.SDL2NativesManager;
import dev.isxander.controlify.utils.Log;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.event.SDL_EventFilter;
import io.github.libsdl4j.api.rwops.SDL_RWops;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.lang3.Validate;

import java.io.InputStream;
import java.util.Optional;

import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.*;
import static io.github.libsdl4j.api.event.SdlEvents.*;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;
import static io.github.libsdl4j.api.rwops.SdlRWops.SDL_RWFromConstMem;

public class SDLControllerManager extends AbstractControllerManager {
    private final Controlify controlify;
    private final Minecraft minecraft;

    private final SDL_Event event = new SDL_Event();

    // must keep a reference to prevent GC from collecting it and the callback failing
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final EventFilter eventFilter;

    public SDLControllerManager() {
        Validate.isTrue(SDL2NativesManager.isLoaded(), "SDL2 natives must be loaded before creating SDLControllerManager");

        this.controlify = Controlify.instance();
        this.minecraft = Minecraft.getInstance();

        SDL_SetEventFilter(eventFilter = new EventFilter(), Pointer.NULL);
    }

    @Override
    public void tick(boolean outOfFocus) {
        while (SDL_PollEvent(event) == 1) {
            switch (event.type) {
                case SDL_JOYDEVICEADDED -> {
                    int jid = event.jdevice.which;
                    Optional<Controller<?, ?>> controllerOpt = createOrGet(jid, controlify.controllerHIDService().fetchType(jid));
                    controllerOpt.ifPresent(controller -> onControllerConnected(controller, true));
                }
                case SDL_CONTROLLERDEVICEADDED -> {
                    int jid = event.cdevice.which;
                    Optional<Controller<?, ?>> controllerOpt = createOrGet(jid, controlify.controllerHIDService().fetchType(jid));
                    controllerOpt.ifPresent(controller -> onControllerConnected(controller, true));
                }

                case SDL_JOYDEVICEREMOVED -> {
                    int jid = event.jdevice.which;
                    getController(jid).ifPresent(this::onControllerRemoved);
                }
                case SDL_CONTROLLERDEVICEREMOVED -> {
                    int jid = event.cdevice.which;
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
    protected void loadGamepadMappings(Resource resource) {
        Log.LOGGER.debug("Loading gamepad mappings...");

        try (InputStream is = resource.open()) {
            byte[] bytes = ByteStreams.toByteArray(is);

            try (Memory memory = new Memory(bytes.length)) {
                memory.write(0, bytes, 0, bytes.length);
                SDL_RWops rw = SDL_RWFromConstMem(memory, bytes.length);
                int count = SDL_GameControllerAddMappingsFromRW(rw, 1);
                if (count < 1) {
                    Log.LOGGER.error("Failed to load gamepad mappings: {}", SDL_GetError());
                }
            }
        } catch (Throwable e) {
            Log.LOGGER.error("Failed to load gamepad mappings", e);
        }
    }

    private static class EventFilter implements SDL_EventFilter {
        @Override
        public int filterEvent(Pointer userdata, SDL_Event event) {
            switch (event.type) {
                case SDL_JOYDEVICEADDED:
                case SDL_JOYDEVICEREMOVED:
                case SDL_CONTROLLERDEVICEADDED:
                case SDL_CONTROLLERDEVICEREMOVED:
                    return 1;
                default:
                    return 0;
            }
        }
    }
}
