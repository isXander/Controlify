package dev.isxander.controlify.driver;

import dev.isxander.controlify.utils.Log;
import io.github.libsdl4j.api.joystick.SDL_Joystick;

import static io.github.libsdl4j.api.error.SdlError.*;
import static io.github.libsdl4j.api.joystick.SdlJoystick.*;

public class SDL2JoystickDriver implements RumbleDriver {
    private final SDL_Joystick ptrJoystick;
    private final boolean isRumbleSupported, isTriggerRumbleSupported;

    public SDL2JoystickDriver(int jid) {
        this.ptrJoystick = SDL_JoystickOpen(jid);
        this.isRumbleSupported = SDL_JoystickHasRumble(ptrJoystick);
        this.isTriggerRumbleSupported = SDL_JoystickHasRumbleTriggers(ptrJoystick);
    }

    @Override
    public void update() {

    }

    @Override
    public boolean rumble(float strongMagnitude, float weakMagnitude) {
        // duration of 0 is infinite
        if (SDL_JoystickRumble(ptrJoystick, (short)(strongMagnitude * 0xFFFF), (short)(weakMagnitude * 0xFFFF), 0) != 0) {
            Log.LOGGER.error("Could not rumble controller: " + SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean rumbleTrigger(float left, float right) {
        // duration of 0 is infinite
        if (SDL_JoystickRumbleTriggers(ptrJoystick, (short)(left * 0xFFFF), (short)(right * 0xFFFF), 0) != 0) {
            Log.LOGGER.error("Could not rumble controller trigger: " + SDL_GetError());
            return false;
        }
        return true;
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
    public String getRumbleDetails() {
        return "SDL2joy supported=" + isRumbleSupported() + " trigger=" + isTriggerRumbleSupported();
    }

    @Override
    public void close() {
        SDL_JoystickClose(ptrJoystick);
    }
}
