package dev.isxander.controlify.controller.joystick;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.AbstractController;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.controller.joystick.mapping.RPJoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.driver.SDL2NativesManager;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.utils.CUtil;
import io.github.libsdl4j.api.joystick.SDL_Joystick;

import java.util.Objects;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;

public class SingleJoystickController extends AbstractController<JoystickState, JoystickConfig> implements JoystickController<JoystickConfig> {
    private JoystickState state = JoystickState.EMPTY, prevState = JoystickState.EMPTY;
    private final JoystickMapping mapping;

    private final SDL_Joystick ptrJoystick;
    private RumbleManager rumbleManager;
    private boolean rumbleSupported;

    public SingleJoystickController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        super(joystickId, hidInfo);

        this.mapping = Objects.requireNonNull(RPJoystickMapping.fromType(this));
        
        this.config = new JoystickConfig(this);
        this.defaultConfig = new JoystickConfig(this);

        this.ptrJoystick = SDL2NativesManager.isLoaded() ? SDL_JoystickOpen(joystickId) : new SDL_Joystick();
        this.rumbleSupported = SDL2NativesManager.isLoaded() && SDL_JoystickHasRumble(this.ptrJoystick);
        this.rumbleManager = new RumbleManager(this);

        this.bindings = new ControllerBindings<>(this);

        this.config.validateRadialActions(bindings);
        this.defaultConfig.validateRadialActions(bindings);
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

        if (mapping() instanceof UnmappedJoystickMapping unmapped) {
            for (int i = 0; i < unmapped.axes().length; i++) {
                unmapped.axes()[i].setTriggerAxis(this.config.isTriggerAxis(i));
            }
        } else {
            for (int i = 0; i < mapping().axes().length; i++) {
                this.config.setTriggerAxis(i, false);
            }
        }
    }

    @Override
    public boolean setRumble(float strongMagnitude, float weakMagnitude) {
        if (!supportsRumble()) return false;

        // the duration doesn't matter because we are not updating the joystick state,
        // so there is never any SDL check to stop the rumble after the desired time.
        if (SDL_JoystickRumble(ptrJoystick, (short)(strongMagnitude * 65535.0F), (short)(weakMagnitude * 65535.0F), 1) != 0) {
            CUtil.LOGGER.error("Could not rumble controller " + name() + ": " + SDL_GetError());
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
        if (!ptrJoystick.equals(new SDL_Joystick()))
            SDL_JoystickClose(ptrJoystick);
        this.rumbleSupported = false;
        this.rumbleManager = null;
    }

    @Override
    public String kind() {
        return "joystick";
    }
}
