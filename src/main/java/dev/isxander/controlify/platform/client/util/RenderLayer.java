package dev.isxander.controlify.platform.client.util;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface RenderLayer
/*? if >1.20.4 {*/ extends net.minecraft.client.gui.LayeredDraw.Layer /*?}*/
{
    void render(GuiGraphics graphics, float tickDelta);

    /*? if >1.20.6 {*/
    /*default void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker timer) {
        this.render(graphics, timer.getGameTimeDeltaPartialTick(false));
    }
    *//*?}*/
}
