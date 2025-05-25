package dev.isxander.controlify.mixins.core;

import dev.isxander.controlify.utils.render.GuiRenderStateSink;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//? if >=1.21.6 {
/*import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
*///?} else {
import net.minecraft.client.renderer.MultiBufferSource;
//?}

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements GuiRenderStateSink {

    //? if >=1.21.6 {
    /*@Shadow @Final private GuiRenderState guiRenderState;

    @Override
    public void controlify$submit(GuiElementRenderState renderState) {
        this.guiRenderState.submitGuiElement(renderState);
    }


    @Shadow @Final private GuiGraphics.ScissorStack scissorStack;

    @Override
    public ScreenRectangle controlify$peekScissorStack() {
        return this.scissorStack.peek();
    }
    *///?} else {
    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;

    @Override
    public MultiBufferSource controlify$bufferSource() {
        return this.bufferSource;
    }
    //?}
}

