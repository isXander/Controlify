package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.hdhaptic.HapticEffects;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;

public class AbstractButtonComponentProcessor implements ComponentProcessor {
    private final AbstractButton button;

    public AbstractButtonComponentProcessor(AbstractButton button) {
        this.button = button;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            controller.hdHaptics().ifPresent(hh -> hh.playHaptic(HapticEffects.NAVIGATE));
            button.playDownSound(Minecraft.getInstance().getSoundManager());
            button.onPress();
            return true;
        }

        return false;
    }
}
