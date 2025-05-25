package dev.isxander.controlify.utils.render;

import net.minecraft.client.gui.GuiGraphics;

//? if >=1.21.6 {
/*import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.navigation.ScreenRectangle;
*///?} else {
import net.minecraft.client.renderer.MultiBufferSource;
//?}

public interface GuiRenderStateSink {
    //? if >=1.21.6 {
    /*void controlify$submit(GuiElementRenderState renderState);

    static void submit(GuiGraphics graphics, GuiElementRenderState renderState) {
        ((GuiRenderStateSink) graphics).controlify$submit(renderState);
    }

    ScreenRectangle controlify$peekScissorStack();

    static ScreenRectangle peekScissorStack(GuiGraphics graphics) {
        return ((GuiRenderStateSink) graphics).controlify$peekScissorStack();
    }
    *///?} else {
    MultiBufferSource controlify$bufferSource();

    static MultiBufferSource bufferSource(GuiGraphics graphics) {
        return ((GuiRenderStateSink) graphics).controlify$bufferSource();
    }
    //?}
}

