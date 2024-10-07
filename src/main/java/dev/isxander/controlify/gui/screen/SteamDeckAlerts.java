package dev.isxander.controlify.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SteamDeckAlerts {
    public static Screen createRunInDesktopOnceMessage(Runnable callback) {
        return createQuitOrContinueScreen(
                callback,
                Component.translatable("controlify.steam_deck.run_in_desktop_once.title"),
                Component.translatable("controlify.steam_deck.run_in_desktop_once.message")
        );
    }

    public static Screen createInitialSetupCompleted() {
        return new AlertScreen(
                () -> Minecraft.getInstance().stop(),
                Component.translatable("controlify.steam_deck.initial_setup_completed.title"),
                Component.translatable("controlify.steam_deck.initial_setup_completed.message"),
                Component.translatable("menu.quit"),
                false
        );
    }

    public static Screen createDesktopModeWarning(Runnable callback) {
        return new AlertScreen(
                callback,
                Component.translatable("controlify.steam_deck.desktop_mode_warning.title"),
                Component.translatable("controlify.steam_deck.desktop_mode_warning.message")
        );
    }

    public static Screen createFailedToCreateCEFFile(Runnable callback) {
        return createQuitOrContinueScreen(
                callback,
                Component.translatable("controlify.steam_deck.failed_to_create_cef_file.title"),
                Component.translatable("controlify.steam_deck.failed_to_create_cef_file.message",
                        Component.literal("https://short.isxander.dev/controlify-deck-guide").withStyle(ChatFormatting.AQUA))
        );
    }

    private static Screen createQuitOrContinueScreen(Runnable callback, Component title, Component message) {
        return new ConfirmScreen(
                (yes) -> {
                    if (yes) {
                        Minecraft.getInstance().stop();
                    } else {
                        callback.run();
                    }
                },
                title,
                message,
                Component.translatable("menu.quit"), // yes
                Component.translatable("gui.continue") // no
        );
    }
}
