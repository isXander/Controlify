package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigOpenerScreen extends Screen {
    private final Screen lastScreen;

    public ModConfigOpenerScreen(Screen lastScreen) {
        super(Component.empty());
        this.lastScreen = lastScreen;
    }

    @Override
    public void added() {
        // need to make sure fabric api has registered all its events
        // because calling setScreen before this will cause fapi to freak
        // out that it has no remove event and crash the whole game lol
        Minecraft minecraft = Minecraft.getInstance();
        this.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());

        Controlify.instance().askNatives()
                .whenComplete((result, error) ->
                        minecraft.setScreen(ControllerCarouselScreen.createConfigScreen(lastScreen))
                );
    }

    @Override
    public void triggerImmediateNarration(boolean useTranslationsCache) {

    }
}
