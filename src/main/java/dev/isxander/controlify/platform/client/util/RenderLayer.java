package dev.isxander.controlify.platform.client.util;

import net.minecraft.client.gui.GuiGraphics;

public interface RenderLayer
/*? if >1.20.4 {*/ extends net.minecraft.client.gui.LayeredDraw.Layer /*?}*/
{
    void render(GuiGraphics graphics, float tickDelta);
}
