package dev.isxander.controlify.driver;

import dev.isxander.controlify.utils.Log;
import org.libsdl.SDL;

public class SDL2JoystickDriver implements RumbleDriver {
    private final long ptrJoystick;
    private final boolean isRumbleSupported;

    public SDL2JoystickDriver(int jid) {
        this.ptrJoystick = SDL.SDL_JoystickOpen(jid);
        this.isRumbleSupported = SDL.SDL_JoystickHasRumble(ptrJoystick);
    }

    @Override
    public void update() {

    }

    @Override
    public boolean rumble(float strongMagnitude, float weakMagnitude) {
        // duration of 0 is infinite
        if (!SDL.SDL_JoystickRumble(ptrJoystick, (int)(strongMagnitude * 65535.0F), (int)(weakMagnitude * 65535.0F), 0)) {
            Log.LOGGER.error("Could not rumble controller: " + SDL.SDL_GetError());
            return false;
        }
        return true;
    }

    @Override
    public boolean isRumbleSupported() {
        return isRumbleSupported;
    }

    @Override
    public String getRumbleDetails() {
        return "SDL2joy supported=" + isRumbleSupported();
    }

    @Override
    public void close() {
        SDL.SDL_JoystickClose(ptrJoystick);
    }
}
