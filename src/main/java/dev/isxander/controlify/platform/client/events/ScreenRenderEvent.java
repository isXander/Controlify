package dev.isxander.controlify.platform.client.events;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ScreenRenderEvent {
    void onRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta);
}
