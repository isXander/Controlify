package dev.isxander.controlify.platform.client.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

public interface RenderLayer
/*? if >1.20.4 { */ extends LayeredDraw.Layer /*?}*/
{
    void render(GuiGraphics graphics, float tickDelta);
}
