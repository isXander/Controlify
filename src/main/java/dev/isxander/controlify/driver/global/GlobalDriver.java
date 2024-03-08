package dev.isxander.controlify.driver.global;

import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.SDL3NativesManager;
import dev.isxander.controlify.utils.CUtil;

public record GlobalDriver(OnScreenKeyboardDriver onScreenKeyboard) {
    private static GlobalDriver instance = null;

    public static GlobalDriver get() {
        if (instance != null) return instance;

        createInstance();
        return instance;
    }

    public static void createInstance() {
        OnScreenKeyboardDriver kbDriver = OnScreenKeyboardDriver.EMPTY;

        // SDL does the exact same thing under the hood, but not sure what else it does...
        if (SteamGlobalDriver.IS_STEAM_DECK) {
            SteamGlobalDriver steamDriver = new SteamGlobalDriver();

            kbDriver = steamDriver;
        }

        if (SDL3NativesManager.isLoaded()) {
            SDLGlobalDriver sdlDriver = new SDLGlobalDriver();

            kbDriver = sdlDriver;
        }

        instance = new GlobalDriver(kbDriver);

        if (DebugProperties.PRINT_DRIVER) {
            CUtil.LOGGER.info(instance.details());
        }
    }

    public String details() {
        return "Global driver: Keyboard = '%s'".formatted(
                this.onScreenKeyboard().keyboardDriverDetails()
        );
    }
}
