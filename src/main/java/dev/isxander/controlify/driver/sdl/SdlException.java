package dev.isxander.controlify.driver.sdl;

import static dev.isxander.sdl3java.api.error.SdlError.SDL_GetError;

public class SdlException extends RuntimeException {
    public SdlException(String message) {
        super(message);
    }

    public static SdlException useSDLError(String message) {
        return new SdlException(message + ": " + SDL_GetError());
    }

    public static SdlException useSDLError() {
        return useSDLError("SDL error");
    }
}
