/*? if sodium {*/
/*package dev.isxander.controlify.compatibility.sodium.screenop;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.compatibility.sodium.mixins.SodiumOptionsGUIAccessor;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlElement;

import java.util.List;

public class SodiumGuiScreenProcessor extends ScreenProcessor<SodiumOptionsGUI> {
    public SodiumGuiScreenProcessor(SodiumOptionsGUI screen) {
        super(screen);
    }

    @Override
    protected void handleComponentNavigation(ControllerEntity controller) {
        super.handleComponentNavigation(controller);
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        var accessor = (SodiumOptionsGUIAccessor) screen;

        if (ControlifyBindings.GUI_NEXT_TAB.on(controller).justPressed()) {
            var currentIndex = accessor.getPages().indexOf(accessor.getCurrentPage());
            var nextIndex = (currentIndex + 1) % accessor.getPages().size();
            screen.setPage(accessor.getPages().get(nextIndex));
        }
        if (ControlifyBindings.GUI_PREV_TAB.on(controller).justPressed()) {
            var currentIndex = accessor.getPages().indexOf(accessor.getCurrentPage());
            var nextIndex = (currentIndex - 1 + accessor.getPages().size()) % accessor.getPages().size();
            screen.setPage(accessor.getPages().get(nextIndex));
        }

        super.handleButtons(controller);
    }

    @Override
    protected void setInitialFocus() {
        if (screen.getFocused() == null && Controlify.instance().currentInputMode().isController() && !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
            List<ControlElement<?>> controls = ((SodiumOptionsGUIAccessor) screen).getControls();
            if (!controls.isEmpty()) {
                var first = controls.get(0);
                screen.setFocused(first);
            }
        }
    }
}
*//*?}*/
