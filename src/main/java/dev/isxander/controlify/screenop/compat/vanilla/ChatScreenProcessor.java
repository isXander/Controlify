package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import net.minecraft.client.gui.screens.ChatScreen;

public class ChatScreenProcessor extends ScreenProcessor<ChatScreen> {
    public ChatScreenProcessor(ChatScreen screen) {
        super(screen);
    }

    @Override
    protected HoldRepeatHelper createHoldRepeatHelper() {
        return new HoldRepeatHelper(3, 4);
    }
}
