package dev.isxander.splitscreen.screenop;

import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * Defines the mode for each screen in the splitscreen.
 * @see ScreenSplitscreenBehaviour
 */
public final class PawnSplitscreenModeRegistry {
    private static final Map<Class<?>, Function<Screen, ScreenSplitscreenMode>> MODES = new HashMap<>();

    public static void init() {
        if (!MODES.isEmpty()) {
            return;
        }

        register(TitleScreen.class, ScreenSplitscreenMode.FULLSCREEN);
        register(PauseScreen.class, ScreenSplitscreenMode.SPLITSCREEN);
        register(ConnectScreen.class, ScreenSplitscreenMode.SPLITSCREEN);
        register(ProgressScreen.class, ScreenSplitscreenMode.SPLITSCREEN);
        register(GenericMessageScreen.class, ScreenSplitscreenMode.SPLITSCREEN);
        register(DisconnectedScreen.class, ScreenSplitscreenMode.FULLSCREEN);
        register(AdvancementsScreen.class, ScreenSplitscreenMode.SPLITSCREEN);
    }

    private static void register(Class<? extends Screen> screenClass, ScreenSplitscreenMode mode) {
        MODES.put(screenClass, screen -> mode);
    }

    private static <T extends Screen> void register(Class<T> screenClass, Function<T, ScreenSplitscreenMode> mode) {
        MODES.put(screenClass, (Function<Screen, ScreenSplitscreenMode>) mode);
    }

    /**
     * Get the splitscreen mode for a given screen.
     * This will check the screen's class and its superclasses until it finds a matching mode.
     * Prioritises the screen's own mode if it implements {@link ScreenSplitscreenBehaviour}.
     * @param screen the screen to check
     * @return the splitscreen mode for the screen
     */
    public static ScreenSplitscreenMode getMode(@Nullable Screen screen) {
        if (screen == null) {
            return ScreenSplitscreenMode.SPLITSCREEN;
        }

        if (screen instanceof ScreenSplitscreenBehaviour behaviour) {
            return behaviour.getSplitscreenMode();
        }

        Function<Screen, ScreenSplitscreenMode> modeSupplier = null;
        Class<?> clazz = screen.getClass();
        while (modeSupplier == null && clazz != null && Screen.class.isAssignableFrom(clazz)) {
            modeSupplier = MODES.get(clazz);
            clazz = clazz.getSuperclass();
        }

        if (modeSupplier == null) {
            return screen.isPauseScreen() ? ScreenSplitscreenMode.FULLSCREEN : ScreenSplitscreenMode.SPLITSCREEN;
        }
        return modeSupplier.apply(screen);
    }
}
