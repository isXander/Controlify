package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

public class JoinMultiplayerScreenProcessor extends ScreenProcessor<JoinMultiplayerScreen> {
    private final ServerSelectionList list;

    public JoinMultiplayerScreenProcessor(JoinMultiplayerScreen screen, ServerSelectionList list) {
        super(screen);
        this.list = list;
    }

    @Override
    protected void handleButtons(Controller controller) {
        if (screen.getFocused() instanceof Button && controller.bindings().GUI_BACK.justPressed()) {
            screen.setFocused(list);
        }

        super.handleButtons(controller);
    }
}
