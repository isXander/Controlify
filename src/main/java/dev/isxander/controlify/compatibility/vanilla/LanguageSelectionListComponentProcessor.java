package dev.isxander.controlify.compatibility.vanilla;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.mixins.compat.screen.vanilla.OptionsSubScreenAccessor;
import net.minecraft.client.Minecraft;

public class LanguageSelectionListComponentProcessor implements ComponentProcessor {
    private final String code;

    public LanguageSelectionListComponentProcessor(String code) {
        this.code = code;
    }

    @Override
    public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller controller) {
        if (controller.bindings().GUI_PRESS.justPressed()) {
            var minecraft = Minecraft.getInstance();
            var languageManager = minecraft.getLanguageManager();
            if (!code.equals(languageManager.getSelected())) {
                languageManager.setSelected(code);
                minecraft.options.languageCode = code;
                minecraft.reloadResourcePacks();
                minecraft.options.save();
            }

            minecraft.setScreen(((OptionsSubScreenAccessor) screen.screen).getLastScreen());

            return true;
        }

        return false;
    }
}
