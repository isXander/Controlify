package dev.isxander.controlify.driver.global;

import dev.isxander.controlify.utils.CUtil;

import static dev.isxander.sdl3java.api.keyboard.SdlKeyboard.*;

public class SDLGlobalDriver implements OnScreenKeyboardDriver {
    private final boolean screenKeyboardSupported;

    public SDLGlobalDriver() {
        screenKeyboardSupported = SDL_HasScreenKeyboardSupport();
        CUtil.LOGGER.info("SDL3 screen keyboard supported: " + screenKeyboardSupported);
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
        return false;
    }

    @Override
    public boolean isOnScreenKeyboardSupported() {
        return screenKeyboardSupported;
    }

    @Override
    public String keyboardDriverDetails() {
        return "SDL3.supported=" + screenKeyboardSupported;
    }
}
