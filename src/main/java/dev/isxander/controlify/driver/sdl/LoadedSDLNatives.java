package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.version.SdlVersionRecord;

import static dev.isxander.sdl3java.api.SdlInit.*;
import static dev.isxander.sdl3java.api.SdlSubSystemConst.*;
import static dev.isxander.sdl3java.api.error.SdlError.*;
import static dev.isxander.sdl3java.api.hints.SdlHintConsts.*;
import static dev.isxander.sdl3java.api.hints.SdlHints.*;
import static dev.isxander.sdl3java.api.version.SdlVersion.*;

/**
 * This class is only constructed in a state where SDL natives are loaded
 * and can be used.
 */
public class LoadedSDLNatives {
    private static final ControlifyLogger logger = CUtil.LOGGER.createSubLogger("LoadedSDLNatives");

    void startSDL3() {
        SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_ENHANCED_REPORTS, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI_STEAM, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_ROG_CHAKRAM, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_ALLOW_BACKGROUND_EVENTS, "1");
        SDL_SetHint(SDL_HINT_JOYSTICK_LINUX_DEADZONES, "1");

        SdlVersionRecord nativesVersion = SdlVersionRecord.fromPacked(SDL_GetVersion());
        SdlVersionRecord javaVersion = SDL_GetJavaBindingsVersion();
        logger.log("Loading SDL3 version: {}. Java bindings targeting: {}", nativesVersion, javaVersion);
        if (!nativesVersion.equals(javaVersion)) {
            logger.warn("SDL3 NATIVE LIBRARY VERSION MISMATCH! Java bindings are targeting a different version of SDL3 than the loaded native library. This may cause issues.");
        }

        // initialise SDL with just joystick and gamecontroller subsystems
        if (!SDL_Init(SDL_INIT_JOYSTICK | SDL_INIT_GAMEPAD | SDL_INIT_EVENTS | SDL_INIT_AUDIO)) {
            CUtil.LOGGER.error("Failed to initialise SDL3: {}", SDL_GetError());
            throw new RuntimeException("Failed to initialise SDL3: " + SDL_GetError());
        }

        logger.log("Successfully initialised SDL subsystems");
    }
}
