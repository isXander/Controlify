package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AskToMapControllerScreen extends ConfirmScreen {
    public AskToMapControllerScreen(ControllerEntity controller, Screen lastScreen) {
        super(
                (confirmed) -> {
                    if (confirmed) {
                        Minecraft.getInstance().setScreen(ControllerMappingMakerScreen.createGamepadMapping(controller.input().orElseThrow(), lastScreen));
                    } else {
                        Minecraft.getInstance().setScreen(lastScreen);
                    }
                },
                Component.translatable("controlify.ask_to_map_controller.title"),
                Component.translatable("controlify.ask_to_map_controller.message"),
                Component.translatable("controlify.ask_to_map_controller.yes"),
                Component.translatable("controlify.ask_to_map_controller.no")
        );
    }
}
