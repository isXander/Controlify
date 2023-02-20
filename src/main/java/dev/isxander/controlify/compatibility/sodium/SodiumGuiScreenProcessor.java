package dev.isxander.controlify.compatibility.sodium;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.sodium.SodiumOptionsGUIAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;

public class SodiumGuiScreenProcessor extends ScreenProcessor<SodiumOptionsGUI> {
    public SodiumGuiScreenProcessor(SodiumOptionsGUI screen) {
        super(screen);
    }

    @Override
    protected void handleComponentNavigation(Controller<?, ?> controller) {
        super.handleComponentNavigation(controller);
    }

    @Override
    protected void handleButtons(Controller<?, ?> controller) {
        var accessor = (SodiumOptionsGUIAccessor) screen;

        if (controller.bindings().GUI_NEXT_TAB.justPressed()) {
            var currentIndex = accessor.getPages().indexOf(accessor.getCurrentPage());
            var nextIndex = (currentIndex + 1) % accessor.getPages().size();
            screen.setPage(accessor.getPages().get(nextIndex));
        }
        if (controller.bindings().GUI_PREV_TAB.justPressed()) {
            var currentIndex = accessor.getPages().indexOf(accessor.getCurrentPage());
            var nextIndex = (currentIndex - 1 + accessor.getPages().size()) % accessor.getPages().size();
            screen.setPage(accessor.getPages().get(nextIndex));
        }

        super.handleButtons(controller);
    }
}
