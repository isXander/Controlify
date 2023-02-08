package dev.isxander.controlify.compatibility.vanilla;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;

public class AbstractButtonComponentProcessor implements ComponentProcessor {
    private final AbstractButton button;

    public AbstractButtonComponentProcessor(AbstractButton button) {
        this.button = button;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            button.playDownSound(Minecraft.getInstance().getSoundManager());
            button.onPress();
            return true;
        }

        return false;
    }
}
