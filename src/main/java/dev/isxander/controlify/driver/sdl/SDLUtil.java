package dev.isxander.controlify.driver.sdl;

import dev.isxander.sdl3java.api.gamepad.SDL_Gamepad;
import dev.isxander.sdl3java.api.joystick.SDL_Joystick;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;

import static dev.isxander.sdl3java.api.gamepad.SdlGamepad.SDL_OpenGamepad;
import static dev.isxander.sdl3java.api.joystick.SdlJoystick.SDL_OpenJoystick;

public class SDLUtil {
    public static SDL_Gamepad openGamepad(SDL_JoystickID jid) {
        SDL_Gamepad gamepad = SDL_OpenGamepad(jid);
        if (gamepad == null) {
            throw SDLException.useSDLError("Failed to open gamepad");
        }
        return gamepad;
    }

    public static SDL_Joystick openJoystick(SDL_JoystickID jid) {
        SDL_Joystick joystick = SDL_OpenJoystick(jid);
        if (joystick == null) {
            throw SDLException.useSDLError("Failed to open joystick");
        }
        return joystick;
    }
}
