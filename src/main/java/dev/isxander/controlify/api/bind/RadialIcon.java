package dev.isxander.controlify.api.bind;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface RadialIcon {
    RadialIcon EMPTY = (graphics, x, y, tickDelta) -> {};

    void draw(GuiGraphicsExtractor graphics, int x, int y, float tickDelta);
}
