package dev.isxander.controlify.controller.sdl2;

import dev.isxander.controlify.Controlify;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import org.libsdl.SDL;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

public class SDL2NativesManager {
    private static final Path NATIVES_FOLDER = FabricLoader.getInstance().getGameDir().resolve("controlify-natives");
    private static final Map<NativeLibrary, String> NATIVE_LIBRARIES = Map.of(
            new NativeLibrary(Util.OS.WINDOWS, true), "windows64/sdl2gdx64.dll",
            new NativeLibrary(Util.OS.WINDOWS, false), "windows32/sdl2gdx.dll",
            new NativeLibrary(Util.OS.LINUX, true), "linux64/libsdl2gdx64.so",
            new NativeLibrary(Util.OS.OSX, true), "macosx64/libsdl2gdx64.dylib"
    );
    private static final String NATIVE_LIBRARY_URL = "https://raw.githubusercontent.com/isXander/sdl2-jni/master/libs/";

    private static Path osNativePath;
    private static boolean loaded = false;

    public static void initialise() {
        if (loaded) return;

        Controlify.LOGGER.info("Initialising SDL2 native library");

        osNativePath = getNativesPathForOS().orElseGet(() -> {
            Controlify.LOGGER.warn("No native library found for SDL2");
            return null;
        });

        if (osNativePath == null) return;

        if (!loadCachedLibrary()) {
            downloadLibrary();

            if (!loadCachedLibrary()) {
                Controlify.LOGGER.warn("Failed to download and load SDL2 native library");
            }
        }
    }

    private static void startSDL2() {
        SDL.SDL_SetHint("SDL_JOYSTICK_ALLOW_BACKGROUND_EVENTS", "1");
        SDL.SDL_SetHint("SDL_ACCELEROMETER_AS_JOYSTICK", "0");
        SDL.SDL_SetHint("SDL_MAC_BACKGROUND_APP", "1");
        SDL.SDL_SetHint("SDL_XINPUT_ENABLED", "1");
        SDL.SDL_SetHint("SDL_JOYSTICK_RAWINPUT", "0");

        int joystickSubsystem = 0x00000200; // implies event subsystem
        if (SDL.SDL_Init(joystickSubsystem) != 0) {
            Controlify.LOGGER.error("Failed to initialise SDL2: " + SDL.SDL_GetError());
            throw new RuntimeException("Failed to initialise SDL2: " + SDL.SDL_GetError());
        }

        Controlify.LOGGER.info("Initialised SDL2");
    }

    private static boolean loadCachedLibrary() {
        if (!Files.exists(osNativePath)) return false;

        Controlify.LOGGER.info("Loading SDL2 native library from " + osNativePath);

        try {
            SDL.load(osNativePath);

            startSDL2();

            loaded = true;
            return true;
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean downloadLibrary() {
        Controlify.LOGGER.info("Downloading SDL2 native library");

        try {
            Files.deleteIfExists(osNativePath);
            Files.createDirectories(osNativePath.getParent());
            Files.createFile(osNativePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try(FileOutputStream fileOutputStream = new FileOutputStream(osNativePath.toFile())) {
            String downloadUrl = NATIVE_LIBRARY_URL + NATIVE_LIBRARIES.get(getNativeLibraryType());
            URL url = new URL(downloadUrl);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            Controlify.LOGGER.info("Downloaded SDL2 native library from " + downloadUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static NativeLibrary getNativeLibraryType() {
        Util.OS os = Util.getPlatform();
        boolean is64bit = System.getProperty("os.arch").contains("64");

        return new NativeLibrary(os, is64bit);
    }

    private static Optional<Path> getNativesPathForOS() {
        String path = NATIVE_LIBRARIES.get(getNativeLibraryType());

        if (path == null) {
            Controlify.LOGGER.warn("No native library found for SDL " + getNativeLibraryType());
            return Optional.empty();
        }

        return Optional.of(NATIVES_FOLDER.resolve(path));
    }

    public static boolean isLoaded() {
        return loaded;
    }

    private record NativeLibrary(Util.OS os, boolean is64Bit) {
    }
}
