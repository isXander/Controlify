package dev.isxander.controlify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SteamDeckAlerts {
    public static Screen createDesktopModeWarning(Runnable callback) {
        return new AlertScreen(
                callback,
                Component.translatable("controlify.steamos_desktop_mode_warning.title"),
                Component.translatable("controlify.steamos_desktop_mode_warning.message")
        );
    }

    public static Screen createCompatInstallRequired(Runnable callback) {
        return new ConfirmScreen(
                (yes) -> {
                    if (yes) {
                        Minecraft.getInstance().stop();
                    } else {
                        callback.run();
                    }
                },
                Component.translatable("controlify.steam_deck_compat_install.title"),
                Component.translatable("controlify.steam_deck_compat_install.message"),
                Component.translatable("menu.quit"), // yes
                Component.translatable("gui.continue") // no
        );
    }
}
