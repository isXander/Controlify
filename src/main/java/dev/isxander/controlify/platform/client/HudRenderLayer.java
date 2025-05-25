package dev.isxander.controlify.platform.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface HudRenderLayer {
    void render(GuiGraphics graphics, DeltaTracker deltaTracker);
}
