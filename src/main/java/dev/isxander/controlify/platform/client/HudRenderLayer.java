package dev.isxander.controlify.platform.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface HudRenderLayer {
    void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
}
