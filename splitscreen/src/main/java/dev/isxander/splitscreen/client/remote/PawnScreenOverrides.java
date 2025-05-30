package dev.isxander.splitscreen.client.remote;

import dev.isxander.splitscreen.client.LocalSplitscreenPawn;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.remote.gui.PawnPauseScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public final class PawnScreenOverrides {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        
        register(PauseScreen.class, (original, minecraft, pawn) -> new PawnPauseScreen(pawn));
    }

    private static <T extends Screen> void register(Class<T> screenClass, ScreenOverrideFactory<T> override) {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            SplitscreenBootstrapper.getPawn().ifPresent(pawn -> {
                if (screenClass.isInstance(screen)) {
                    Screen newScreen = override.create(screenClass.cast(screen), client, pawn.getPawn());
                    client.setScreen(newScreen);
                }
            });
        });
    }

    public interface ScreenOverrideFactory<T extends Screen> {
        @Nullable Screen create(T original, Minecraft minecraft, LocalSplitscreenPawn pawn);
    }

    private PawnScreenOverrides() {
    }
}
