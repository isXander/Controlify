package dev.isxander.controlify.screenkeyboard;

import net.minecraft.client.gui.screens.ChatScreen;

public interface ChatKeyboardDucky {
    boolean controlify$hasKeyboard();

    static boolean hasKeyboard(ChatScreen screen) {
        return ((ChatKeyboardDucky) screen).controlify$hasKeyboard();
    }
}
