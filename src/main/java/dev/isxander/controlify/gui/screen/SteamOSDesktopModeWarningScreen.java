package dev.isxander.controlify.gui.screen;

import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.network.chat.Component;

public class SteamOSDesktopModeWarningScreen extends AlertScreen {
    public SteamOSDesktopModeWarningScreen(Runnable callback) {
        super(
                callback,
                Component.translatable("controlify.steamos_desktop_mode_warning.title"),
                Component.translatable("controlify.steamos_desktop_mode_warning.message")
        );
    }
}
