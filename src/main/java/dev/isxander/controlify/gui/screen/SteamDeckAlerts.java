package dev.isxander.controlify.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SteamDeckAlerts {

    public static Screen createDesktopModeWarning(Runnable callback) {
        return createQuitOrContinueScreen(
                callback,
                Component.translatable("controlify.steam_deck.desktop_mode_warning.title"),
                Component.translatable("controlify.steam_deck.desktop_mode_warning.message")
        );
    }

    public static Screen createDeckyRequiredWarning(Runnable callback) {
        return createQuitOrContinueScreen(
                callback,
                Component.translatable("controlify.steam_deck.decky_required_warning.title"),
                Component.translatable("controlify.steam_deck.decky_required_warning.message",
                        Component.literal("https://short.isxander.dev/decky-install").withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE))
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
