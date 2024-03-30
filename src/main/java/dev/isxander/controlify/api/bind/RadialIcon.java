package dev.isxander.controlify.api.bind;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface RadialIcon {
    RadialIcon EMPTY = (graphics, x, y, tickDelta) -> {};

    void draw(GuiGraphics graphics, int x, int y, float tickDelta);
}
