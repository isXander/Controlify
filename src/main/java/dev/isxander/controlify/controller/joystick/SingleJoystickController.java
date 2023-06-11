package dev.isxander.controlify.controller.joystick;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.controller.joystick.mapping.RPJoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.sdl2.SDL2NativesManager;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.utils.Log;
import org.libsdl.SDL;

import java.util.Objects;

public class SingleJoystickController extends AbstractController<JoystickState, JoystickConfig> implements JoystickController<JoystickConfig> {
    private JoystickState state = JoystickState.EMPTY, prevState = JoystickState.EMPTY;
    private final JoystickMapping mapping;

    private long ptrJoystick;
    private RumbleManager rumbleManager;
    private boolean rumbleSupported;

    public SingleJoystickController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        super(joystickId, hidInfo);

        this.mapping = Objects.requireNonNull(RPJoystickMapping.fromType(this));
        
        this.config = new JoystickConfig(this);
        this.defaultConfig = new JoystickConfig(this);

        this.ptrJoystick = SDL2NativesManager.isLoaded() ? SDL.SDL_JoystickOpen(joystickId) : 0;
        this.rumbleSupported = SDL2NativesManager.isLoaded() && SDL.SDL_JoystickHasRumble(this.ptrJoystick);
        this.rumbleManager = new RumbleManager(this);

        this.bindings = new ControllerBindings<>(this);
    }

    @Override
    public JoystickState state() {
        return state;
    }

    @Override
    public JoystickState prevState() {
        return prevState;
    }

    @Override
    public void updateState() {
        prevState = state;
        state = JoystickState.fromJoystick(this, joystickId);
    }

    @Override
    public void clearState() {
        this.state = JoystickState.empty(this);
    }

    @Override
    public JoystickMapping mapping() {
        return mapping;
    }

    @Override
    public int axisCount() {
        return mapping().axes().length;
    }

    @Override
    public int buttonCount() {
        return mapping.buttons().length;
    }

    @Override
    public int hatCount() {
        return mapping.hats().length;
    }

    @Override
    public int joystickId() {
        return joystickId;
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        super.setConfig(gson, json);
        this.config.setup(this);
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude, RumbleSource source) {
        if (!supportsRumble()) return false;

        var strengthMod = config().getRumbleStrength(source);
        if (source != RumbleSource.MASTER)
            strengthMod *= config().getRumbleStrength(RumbleSource.MASTER);

        strongMagnitude *= strengthMod;
        weakMagnitude *= strengthMod;

        // the duration doesn't matter because we are not updating the joystick state,
        // so there is never any SDL check to stop the rumble after the desired time.
        if (!SDL.SDL_JoystickRumbleTriggers(ptrJoystick, (int)(strongMagnitude * 65535.0F), (int)(weakMagnitude * 65535.0F), 1)) {
            Log.LOGGER.error("Could not rumble controller " + name() + ": " + SDL.SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean supportsRumble() {
        return rumbleSupported;
    }

    @Override
    public RumbleManager rumbleManager() {
        return this.rumbleManager;
    }

    @Override
    public void close() {
        SDL.SDL_JoystickClose(ptrJoystick);
        this.rumbleSupported = false;
        this.rumbleManager = null;
    }
}
