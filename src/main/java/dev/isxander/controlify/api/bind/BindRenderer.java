package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiGraphics;

public interface BindRenderer {
    DrawSize size();

    void render(GuiGraphics graphics, int x, int centerY);
}
