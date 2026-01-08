package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Platform;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.jna.SdlNativeLibraryLoader;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

public final class SDLNativesLoader {
    private static final ControlifyLogger logger = CUtil.LOGGER.createSubLogger("SDLNativesLoader");

    private static boolean hasAttemptedLoad = false;
    private static @Nullable LoadedSDLNatives loadedSDLNatives;

    public static Optional<LoadedSDLNatives> get() {
        return Optional.ofNullable(loadedSDLNatives);
    }

    public static boolean hasAttemptedLoad() {
        return hasAttemptedLoad;
    }

    public static boolean isLoaded() {
        return loadedSDLNatives != null;
    }

    public static Optional<LoadedSDLNatives> getOrLoad() {
        tryLoad();
        return get();
    }

    /**
     * @return true if SDL successfully loaded, false otherwise
     */
    public static boolean tryLoad() {
        if (hasAttemptedLoad) {
            return isLoaded();
        }

        hasAttemptedLoad = true;

        try {
            String path = SdlNativeLibraryLoader.SDL_LIBRARY_NAME;
            if (CUtil.IS_POJAV_LAUNCHER) {
                logger.log("Detected PojavLauncher.");
                String nativesFolderName = System.getenv("POJAV_NATIVEDIR");
                Path libsLocation = Path.of(nativesFolderName).toAbsolutePath();
                path = libsLocation.resolve("libSDL3.so").toString();
            }

            logger.log("Attempting to load SDL3 from {}", path);

            SdlNativeLibraryLoader.loadLibSDL3FromFilePathNow(path);

            loadedSDLNatives = new LoadedSDLNatives();
            loadedSDLNatives.startSDL3();
            logger.log("Successfully loaded SDL3 natives");

            return true;
        } catch (UnsatisfiedLinkError e) {
            logger.error("Failed to find SDL. Need {}", e, Platform.RESOURCE_PREFIX);
        } catch (Throwable e) {
            logger.error("Failed to load SDL natives", e);
        }

        return false;
    }
}
