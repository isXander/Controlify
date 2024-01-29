package dev.isxander.controlify.driver.joystick;

import dev.isxander.controlify.controller.BatteryLevel;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.driver.BatteryDriver;
import dev.isxander.controlify.driver.GUIDProvider;
import dev.isxander.controlify.driver.NameProviderDriver;
import dev.isxander.controlify.driver.RumbleDriver;
import dev.isxander.controlify.utils.CUtil;
import io.github.libsdl4j.api.joystick.SDL_Joystick;
import net.minecraft.util.Mth;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.joystick.SDL_JoystickPowerLevel.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;
import static io.github.libsdl4j.api.joystick.SdlJoystickConst.*;

public class SDL2JoystickDriver implements BasicJoystickInputDriver, RumbleDriver, BatteryDriver, GUIDProvider, NameProviderDriver {
    private final SDL_Joystick ptrJoystick;
    private BasicJoystickState state = BasicJoystickState.EMPTY;
    private final boolean isRumbleSupported, isTriggerRumbleSupported;
    private final String guid;
    private final String name;

    private final int numAxes, numButtons, numHats;

    public SDL2JoystickDriver(int jid) {
        this.ptrJoystick = SDL_JoystickOpen(jid);
        if (ptrJoystick == null)
            throw new IllegalStateException("Could not open joystick: " + SDL_GetError());

        this.guid = SDL_JoystickGetGUID(ptrJoystick).toString();
        this.name = SDL_JoystickName(ptrJoystick);
        this.isRumbleSupported = SDL_JoystickHasRumble(ptrJoystick);
        this.isTriggerRumbleSupported = SDL_JoystickHasRumbleTriggers(ptrJoystick);

        this.numAxes = SDL_JoystickNumAxes(ptrJoystick);
        this.numButtons = SDL_JoystickNumButtons(ptrJoystick);
        this.numHats = SDL_JoystickNumHats(ptrJoystick);
    }

    @Override
    public void update() {
        SDL_JoystickUpdate();

        float[] axes = new float[numAxes];
        for (int i = 0; i < numAxes; i++) {
            axes[i] = mapShortToFloat(SDL_JoystickGetAxis(ptrJoystick, i));
        }

        boolean[] buttons = new boolean[numButtons];
        for (int i = 0; i < numButtons; i++) {
            buttons[i] = SDL_JoystickGetButton(ptrJoystick, i) == 1;
        }

        JoystickState.HatState[] hats = new JoystickState.HatState[numHats];
        for (int i = 0; i < numHats; i++) {

            hats[i] = switch(SDL_JoystickGetHat(ptrJoystick, i)) {
                case SDL_HAT_CENTERED -> JoystickState.HatState.CENTERED;
                case SDL_HAT_UP -> JoystickState.HatState.UP;
                case SDL_HAT_RIGHT -> JoystickState.HatState.RIGHT;
                case SDL_HAT_DOWN -> JoystickState.HatState.DOWN;
                case SDL_HAT_LEFT -> JoystickState.HatState.LEFT;
                case SDL_HAT_RIGHTUP -> JoystickState.HatState.RIGHT_UP;
                case SDL_HAT_RIGHTDOWN -> JoystickState.HatState.RIGHT_DOWN;
                case SDL_HAT_LEFTUP -> JoystickState.HatState.LEFT_UP;
                case SDL_HAT_LEFTDOWN -> JoystickState.HatState.LEFT_DOWN;
                default -> throw new IllegalStateException("Unexpected value: " + SDL_JoystickGetHat(ptrJoystick, i));
            };
        }

        this.state = new BasicJoystickState(buttons, axes, hats);
    }

    @Override
    public int getNumAxes() {
        return numAxes;
    }

    @Override
    public int getNumButtons() {
        return numButtons;
    }

    @Override
    public int getNumHats() {
        return numHats;
    }

    @Override
    public BasicJoystickState getBasicJoystickState() {
        return this.state;
    }

    @Override
    public boolean rumble(float strongMagnitude, float weakMagnitude) {
        if (!isRumbleSupported()) return false;

        // duration of 0 is infinite
        if (SDL_JoystickRumble(ptrJoystick, (short)(strongMagnitude * 0xFFFF), (short)(weakMagnitude * 0xFFFF), 0) != 0) {
            CUtil.LOGGER.error("Could not rumble controller: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean rumbleTrigger(float left, float right) {
        if (!isTriggerRumbleSupported()) return false;

        // duration of 0 is infinite
        if (SDL_JoystickRumbleTriggers(ptrJoystick, (short)(left * 0xFFFF), (short)(right * 0xFFFF), 0) != 0) {
            CUtil.LOGGER.error("Could not rumble controller trigger: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public BatteryLevel getBatteryLevel() {
        int powerLevel = SDL_JoystickCurrentPowerLevel(ptrJoystick);
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
    public String getBasicJoystickDetails() {
        return "SDL2joy axes=" + numAxes + " buttons=" + numButtons + " hats=" + numHats;
    }

    @Override
    public String getRumbleDetails() {
        return "SDL2joy supported=" + isRumbleSupported() + " trigger=" + isTriggerRumbleSupported();
    }

    @Override
    public String getGUIDProviderDetails() {
        return "SDL2joy";
    }

    @Override
    public String getBatteryDriverDetails() {
        return "SDL2joy";
    }

    @Override
    public String getNameProviderDetails() {
        return "SDL2joy";
    }

    @Override
    public void close() {
        SDL_JoystickClose(ptrJoystick);
    }

    private static float mapShortToFloat(short value) {
        return Mth.clampedMap(value, Short.MIN_VALUE, 0, -1f, 0f)
                + Mth.clampedMap(value, 0, Short.MAX_VALUE, 0f, 1f);
    }
}
