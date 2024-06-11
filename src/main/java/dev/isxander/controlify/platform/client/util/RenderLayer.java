package dev.isxander.controlify.platform.client.util;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RenderLayer
    //? if >1.20.4 {
    extends net.minecraft.client.gui.LayeredDraw.Layer
    //?} elif neoforge {
    /*extends net.neoforged.neoforge.client.gui.overlay.IGuiOverlay
    *///?}
{
    void render(GuiGraphics graphics, float tickDelta);

    //? if >1.20.6 {
    /*default void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker timer) {
        this.render(graphics, timer.getGameTimeDeltaPartialTick(false));
    }
    *///?}

    //? if <=1.20.4 && neoforge {
    /*default void render(@NotNull net.neoforged.neoforge.client.gui.overlay.ExtendedGui gui, @NotNull GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        this.render(graphics, partialTick);
    }
    *///?}
}
