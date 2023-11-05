package dev.isxander.controlify.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class NoSDLScreen extends AlertScreen {
    public NoSDLScreen(Runnable actionHandler, Screen parent) {
        super(
                () -> {
                    actionHandler.run();
                    Minecraft.getInstance().setScreen(parent);
                },
                Component.translatable("controlify.gui.no_sdl.title"),
                Component.translatable("controlify.gui.no_sdl.message"),
                CommonComponents.GUI_OK,
                false
        );
    }
}
