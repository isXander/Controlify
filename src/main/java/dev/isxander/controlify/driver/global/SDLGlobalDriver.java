package dev.isxander.controlify.driver.global;

import dev.isxander.controlify.utils.CUtil;

import static io.github.libsdl4j.api.keyboard.SdlKeyboard.*;

public class SDLGlobalDriver implements OnScreenKeyboardDriver {
    private final boolean screenKeyboardSupported;

    public SDLGlobalDriver() {
        screenKeyboardSupported = SDL_HasScreenKeyboardSupport();
        CUtil.LOGGER.info("SDL2 screen keyboard supported: " + screenKeyboardSupported);
    }

    @Override
    public void openOnScreenKeyboard(int obstructionX, int obstructionY, int obstructionWidth, int obstructionHeight) {
        if (screenKeyboardSupported) {
            SDL_StartTextInput();
        } else {
            CUtil.LOGGER.warn("Attempted to open on-screen keyboard when it is not supported!");
        }
    }

    @Override
    public void closeOnScreenKeyboard() {
        SDL_StopTextInput();
    }

    @Override
    public boolean isKeyboardShown() {
        return SDL_IsScreenKeyboardShown(null);
    }

    @Override
    public boolean isOnScreenKeyboardSupported() {
        return screenKeyboardSupported;
    }

    @Override
    public String keyboardDriverDetails() {
        return "SDL2.supported=" + screenKeyboardSupported;
    }
}
