package dev.isxander.splitscreen.client.host.gui;

import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SplitscreenDisconnectedScreen extends DisconnectedScreen {

    public SplitscreenDisconnectedScreen(Screen parent, Component title, Component reason) {
        super(parent, title, reason);
    }

}
