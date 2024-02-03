package dev.isxander.controlify.driver.joystick;

import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.controller.composable.ComposableControllerState;
import dev.isxander.controlify.controller.composable.ModifiableControllerState;
import dev.isxander.controlify.controller.composable.impl.ComposableControllerStateImpl;
import dev.isxander.controlify.controller.composable.joystick.JoystickInputs;
import dev.isxander.controlify.controller.composable.HatState;
import dev.isxander.controlify.driver.*;
import dev.isxander.controlify.utils.CUtil;
import io.github.libsdl4j.api.joystick.SDL_Joystick;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;
import io.github.libsdl4j.api.properties.SDL_PropertiesID;
import net.minecraft.util.Mth;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;
import static io.github.libsdl4j.api.joystick.SDL_JoystickPowerLevel.*;
import static io.github.libsdl4j.api.joystick.SdlJoystickHatConst.*;
import static io.github.libsdl4j.api.joystick.SdlJoystickPropsConst.*;
import static io.github.libsdl4j.api.properties.SdlProperties.SDL_GetBooleanProperty;

public class SDL3JoystickDriver implements InputDriver, RumbleDriver, BatteryDriver, GUIDProvider, NameProviderDriver {
    private final SDL_Joystick ptrJoystick;
    private ComposableControllerState state = ComposableControllerState.EMPTY;
    private final boolean isRumbleSupported, isTriggerRumbleSupported;
    private final String guid;
    private final String name;

    private final int numAxes, numButtons, numHats;

    public SDL3JoystickDriver(SDL_JoystickID jid) {
        this.ptrJoystick = SDL_OpenJoystick(jid);
        if (ptrJoystick == null)
            throw new IllegalStateException("Could not open joystick: " + SDL_GetError());

        SDL_PropertiesID props = SDL_GetJoystickProperties(ptrJoystick);

        this.guid = SDL_GetJoystickInstanceGUID(jid).toString();
        this.name = SDL_GetJoystickName(ptrJoystick);
        this.isRumbleSupported = SDL_GetBooleanProperty(props, SDL_PROP_JOYSTICK_CAP_RUMBLE_BOOLEAN, false);
        this.isTriggerRumbleSupported = SDL_GetBooleanProperty(props, SDL_PROP_JOYSTICK_CAP_TRIGGER_RUMBLE_BOOLEAN, false);

        this.numAxes = SDL_GetNumJoystickAxes(ptrJoystick);
        this.numButtons = SDL_GetNumJoystickButtons(ptrJoystick);
        this.numHats = SDL_GetNumJoystickHats(ptrJoystick);
    }

    @Override
    public void update() {
        ModifiableControllerState state = new ComposableControllerStateImpl();

        for (int i = 0; i < numAxes; i++) {
            state.setAxis(JoystickInputs.axis(i), mapShortToFloat(SDL_GetJoystickAxis(ptrJoystick, i)));
        }

        for (int i = 0; i < numButtons; i++) {
            state.setButton(JoystickInputs.button(i), SDL_GetJoystickButton(ptrJoystick, i) == 1);
        }

        for (int i = 0; i < numHats; i++) {
            HatState hatState = switch(SDL_GetJoystickHat(ptrJoystick, i)) {
                case SDL_HAT_CENTERED -> HatState.CENTERED;
                case SDL_HAT_UP -> HatState.UP;
                case SDL_HAT_RIGHT -> HatState.RIGHT;
                case SDL_HAT_DOWN -> HatState.DOWN;
                case SDL_HAT_LEFT -> HatState.LEFT;
                case SDL_HAT_RIGHTUP -> HatState.RIGHT_UP;
                case SDL_HAT_RIGHTDOWN -> HatState.RIGHT_DOWN;
                case SDL_HAT_LEFTUP -> HatState.LEFT_UP;
                case SDL_HAT_LEFTDOWN -> HatState.LEFT_DOWN;
                default -> throw new IllegalStateException("Unexpected value: " + SDL_GetJoystickHat(ptrJoystick, i));
            };

            state.setHat(JoystickInputs.hat(i), hatState);
        }

        this.state = state;
    }

    @Override
    public ComposableControllerState getInputState() {
        return state;
    }

    @Override
    public boolean isGyroSupported() {
        return false;
    }

    @Override
    public int numButtons() {
        return SDL_GetNumJoystickButtons(ptrJoystick);
    }

    @Override
    public int numAxes() {
        return SDL_GetNumJoystickAxes(ptrJoystick);
    }

    @Override
    public int numHats() {
        return SDL_GetNumJoystickHats(ptrJoystick);
    }

    @Override
    public boolean rumble(float strongMagnitude, float weakMagnitude) {
        if (!isRumbleSupported()) return false;

        // duration of 0 is infinite
        if (SDL_RumbleJoystick(ptrJoystick, (short)(strongMagnitude * 0xFFFF), (short)(weakMagnitude * 0xFFFF), 0) != 0) {
            CUtil.LOGGER.error("Could not rumble controller: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean rumbleTrigger(float left, float right) {
        if (!isTriggerRumbleSupported()) return false;

        // duration of 0 is infinite
        if (SDL_RumbleJoystickTriggers(ptrJoystick, (short)(left * 0xFFFF), (short)(right * 0xFFFF), 0) != 0) {
            CUtil.LOGGER.error("Could not rumble controller trigger: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public BatteryLevel getBatteryLevel() {
        int powerLevel = SDL_GetJoystickPowerLevel(ptrJoystick);
        return switch (powerLevel) {
            case SDL_JOYSTICK_POWER_UNKNOWN -> BatteryLevel.UNKNOWN;
            case SDL_JOYSTICK_POWER_EMPTY -> BatteryLevel.EMPTY;
            case SDL_JOYSTICK_POWER_LOW -> BatteryLevel.LOW;
            case SDL_JOYSTICK_POWER_MEDIUM -> BatteryLevel.MEDIUM;
            case SDL_JOYSTICK_POWER_FULL -> BatteryLevel.FULL;
            case SDL_JOYSTICK_POWER_WIRED -> BatteryLevel.WIRED;
            case SDL_JOYSTICK_POWER_MAX -> BatteryLevel.MAX;
            default -> throw new IllegalStateException("Unexpected value: " + powerLevel);
        };
    }

    @Override
    public boolean isRumbleSupported() {
        return isRumbleSupported;
    }

    @Override
    public boolean isTriggerRumbleSupported() {
        return isTriggerRumbleSupported;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getGUID() {
        return guid;
    }

    @Override
    public String getInputDriverDetails() {
        return "SDL3joy axes=" + numAxes + " buttons=" + numButtons + " hats=" + numHats;
    }

    @Override
    public String getRumbleDetails() {
        return "SDL3joy supported=" + isRumbleSupported() + " trigger=" + isTriggerRumbleSupported();
    }

    @Override
    public String getGUIDProviderDetails() {
        return "SDL3joy";
    }

    @Override
    public String getBatteryDriverDetails() {
        return "SDL3joy";
    }

    @Override
    public String getNameProviderDetails() {
        return "SDL3joy";
    }

    @Override
    public void close() {
        SDL_CloseJoystick(ptrJoystick);
    }

    private static float mapShortToFloat(short value) {
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }
}
