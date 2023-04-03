package dev.isxander.controlify.controller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.controller.sdl2.SDL2NativesManager;
import dev.isxander.controlify.rumble.RumbleCapable;
import dev.isxander.controlify.rumble.RumbleManager;
import org.libsdl.SDL;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractController<S extends ControllerState, C extends ControllerConfig> implements Controller<S, C>, RumbleCapable {
    protected final int joystickId;
    protected String name;
    private final String uid;
    private final String guid;
    private final ControllerType type;
    private final long ptrJoystick;
    private final RumbleManager rumbleManager;

    private final ControllerBindings<S> bindings;
    protected C config, defaultConfig;

    public AbstractController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        if (joystickId > GLFW.GLFW_JOYSTICK_LAST || joystickId < 0)
            throw new IllegalArgumentException("Joystick ID " + joystickId + " is out of range!");
        if (!GLFW.glfwJoystickPresent(joystickId))
            throw new IllegalArgumentException("Joystick " + joystickId + " is not present and cannot be initialised!");

        this.joystickId = joystickId;
        this.guid = GLFW.glfwGetJoystickGUID(joystickId);
        this.ptrJoystick = SDL.SDL_JoystickOpen(joystickId);
        this.rumbleManager = new RumbleManager(this);

        if (hidInfo.path().isPresent()) {
            this.uid = UUID.nameUUIDFromBytes(hidInfo.path().get().getBytes()).toString();
        } else {
            this.uid = "unidentified-guid-" + UUID.nameUUIDFromBytes(this.guid.getBytes());
        }
        this.type = hidInfo.type();

        var joystickName = GLFW.glfwGetJoystickName(joystickId);
        String name = type != ControllerType.UNKNOWN || joystickName == null ? type.friendlyName() : joystickName;
        setName(name);

        this.bindings = new ControllerBindings<>(this);
    }

    public String name() {
        if (config().customName != null)
            return config().customName;
        return name;
    }

    protected void setName(String name) {
        String uniqueName = name;
        int i = 0;
        while (CONTROLLERS.values().stream().map(Controller::name).anyMatch(uniqueName::equalsIgnoreCase)) {
            uniqueName = name + " (" + i++ + ")";
            if (i > 1000) throw new IllegalStateException("Could not find a unique name for controller " + name + " (" + uid() + ")! (tried " + i + " times)");
        }
        this.name = uniqueName;
    }

    @Override
    public String uid() {
        return this.uid;
    }

    @Override
    public int joystickId() {
        return this.joystickId;
    }

    @Override
    public ControllerType type() {
        return this.type;
    }

    @Override
    public ControllerBindings<S> bindings() {
        return this.bindings;
    }

    @Override
    public C config() {
        return this.config;
    }

    @Override
    public C defaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public void resetConfig() {
        this.config = defaultConfig();
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        C newConfig = gson.fromJson(json, new TypeToken<C>(getClass()){}.getType());
        if (newConfig != null) {
            this.config = newConfig;
        } else {
            Controlify.LOGGER.error("Could not set config for controller " + name() + " (" + uid() + ")! Using default config instead.");
            this.config = defaultConfig();
        }
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude) {
        if (!canRumble()) return false;

        // the duration doesn't matter because we are not updating the joystick state,
        // so there is never any SDL check to stop the rumble after the desired time.
        if (!SDL.SDL_JoystickRumble(ptrJoystick, (int)(strongMagnitude * 65535.0F), (int)(weakMagnitude * 65535.0F), 1)) {
            Controlify.LOGGER.error("Could not rumble controller " + name() + ": " + SDL.SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean canRumble() {
        return SDL2NativesManager.isLoaded() && config().allowVibrations;
    }

    @Override
    public RumbleManager rumbleManager() {
        return this.rumbleManager;
    }

    @Override
    public void close() {
        SDL.SDL_JoystickClose(ptrJoystick);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractController<?, ?> that = (AbstractController<?, ?>) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
