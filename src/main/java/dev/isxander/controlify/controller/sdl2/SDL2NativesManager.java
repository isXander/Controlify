package dev.isxander.controlify.controller.sdl2;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.utils.DebugLog;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import org.libsdl.SDL;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.libsdl.SDL_Hints.*;

public class SDL2NativesManager {
    private static final String SDL2_VERSION = "<SDL2_VERSION>";
    private static final Map<Target, String> NATIVE_LIBRARIES = Map.of(
            new Target(Util.OS.WINDOWS, true), "windows64.dll",
            new Target(Util.OS.WINDOWS, false), "window32.dll",
            new Target(Util.OS.LINUX, true), "linux64.so"
            //new Target(Util.OS.OSX, true), "mac64.dylib"
    );
    private static final String NATIVE_LIBRARY_URL = "https://maven.isxander.dev/releases/dev/isxander/sdl2-jni-natives/%s/".formatted(SDL2_VERSION);

    private static boolean loaded = false;

    public static void initialise() {
        if (loaded) return;

        DebugLog.log("Initialising SDL2 native library");

        if (!Target.CURRENT.hasNativeLibrary()) {
            Controlify.LOGGER.warn("SDL2 native library not available for OS: " + Target.CURRENT);
            return;
        }

        Path localLibraryPath = Target.CURRENT.getLocalNativePath();
        if (Files.notExists(localLibraryPath)) {
            Controlify.LOGGER.info("Downloading SDL2 native library: " + Target.CURRENT.getArtifactName());
            downloadLibrary(localLibraryPath);
        }

        try {
            SDL.load(localLibraryPath);

            startSDL2();

            loaded = true;
        } catch (Exception e) {
            Controlify.LOGGER.error("Failed to load SDL2 native library", e);
        }
    }

    private static void startSDL2() {
        // we have no windows, so all events are background events
        SDL.SDL_SetHint(SDL_HINT_JOYSTICK_ALLOW_BACKGROUND_EVENTS, "1");
        // accelerometer as joystick is not good UX. unexpected
        SDL.SDL_SetHint(SDL_HINT_ACCELEROMETER_AS_JOYSTICK, "0");
        // see first hint
        SDL.SDL_SetHint(SDL_HINT_MAC_BACKGROUND_APP, "1");
        // raw input requires controller correlation, which is impossible
        // without calling JoystickUpdate, which we don't do.
        SDL.SDL_SetHint(SDL_HINT_JOYSTICK_RAWINPUT, "0");
        // better rumble
        SDL.SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI_PS4_RUMBLE, "1");
        SDL.SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI_PS5_RUMBLE, "1");
        SDL.SDL_SetHint(SDL_HINT_JOYSTICK_HIDAPI_STEAM, "1");

        int joystickSubsystem = 0x00000200; // implies event subsystem
        int gameControllerSubsystem = 0x00002000; // implies event subsystem
        if (SDL.SDL_Init(joystickSubsystem | gameControllerSubsystem) != 0) {
            Controlify.LOGGER.error("Failed to initialise SDL2: " + SDL.SDL_GetError());
            throw new RuntimeException("Failed to initialise SDL2: " + SDL.SDL_GetError());
        }

        DebugLog.log("Initialised SDL2");
    }

    private static boolean downloadLibrary(Path path) {
        try {
            Files.deleteIfExists(path);
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try(FileOutputStream fileOutputStream = new FileOutputStream(path.toFile())) {
            String url = NATIVE_LIBRARY_URL + Target.CURRENT.getArtifactName();
            URL downloadUrl = new URL(url);
            ReadableByteChannel readableByteChannel = Channels.newChannel(downloadUrl.openStream());
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            Controlify.LOGGER.info("Downloaded SDL2 native library from " + downloadUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    private record Target(Util.OS os, boolean is64Bit) {
        public static final Target CURRENT = Util.make(() -> {
            Util.OS os = Util.getPlatform();
            boolean is64bit = System.getProperty("os.arch").contains("64");

            return new Target(os, is64bit);
        });

        public boolean hasNativeLibrary() {
            return NATIVE_LIBRARIES.containsKey(this);
        }

        public String getArtifactName() {
            String suffix = NATIVE_LIBRARIES.get(Target.CURRENT);
            return "sdl2-jni-natives-" + SDL2_VERSION + "-" + suffix;
        }

        public Path getLocalNativePath() {
            return FabricLoader.getInstance().getGameDir()
                    .resolve("controlify-natives")
                    .resolve(getArtifactName());
        }
    }
}
