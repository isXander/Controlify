package dev.isxander.controlify.screenkeyboard;

import net.minecraft.client.gui.screens.ChatScreen;

public interface ChatKeyboardDucky {
    float controlify$keyboardShiftAmount();

    static float getKeyboardShiftAmount(ChatScreen screen) {
        return ((ChatKeyboardDucky) screen).controlify$keyboardShiftAmount();
    }
}
