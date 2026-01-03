package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

//? if >=1.21.6 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
//?}

public interface CGuiElementRenderState /*? if >=1.21.6 {*/extends GuiElementRenderState /*?}*/ {

    BaseRenderState baseState();

    //? if >=1.21.9 {
    @Override
    default void buildVertices(VertexConsumer vertexConsumer) {
        this.buildVertices(vertexConsumer, 0);
    }

    // purely to ease development, we backport this method from <1.21.9 and have a placeholder Z
    void buildVertices(VertexConsumer vertexConsumer, float z);
    //?}

    //? if >=1.21.6 {
    @Override
    default @NotNull RenderPipeline pipeline() {
        return baseState().pipeline();
    }

    @Override
    default @NotNull TextureSetup textureSetup() {
        return baseState().textureSetup();
    }

    @Override
    default @Nullable ScreenRectangle scissorArea() {
        return baseState().scissorArea();
    }

    @Override
    default @Nullable ScreenRectangle bounds() {
        return baseState().bounds();
    }
    //?} else {
    /*void buildVertices(VertexConsumer vertexConsumer, float z);
    *///?}

    default VertexConsumer add2DVertex(
            VertexConsumer vertexConsumer,
            float x, float y, float z
    ) {
        return add2DVertex(vertexConsumer, baseState().pose(), x, y, z);
    }

    default VertexConsumer add2DVertex(
            VertexConsumer vertexConsumer,
            /*? if >=1.21.6 {*/ Matrix3x2f /*?} else {*/ /*Matrix4f *//*?}*/ pose,
            float x, float y, float z
    ) {
        //? if >=1.21.6 {
        return vertexConsumer.addVertexWith2DPose(pose, x, y /*? if <1.21.9 >>*//*,z*/ );
        //?} else {
        /*return vertexConsumer.addVertex(pose, x, y, z);
         *///?}
    }

    default void submit(GuiGraphics graphics) {
        //? if >=1.21.6 {
        GuiRenderStateSink.submit(graphics, this);
         //?} else {
        /*// don't use drawSpecial since it flushes changes, preventing batches
        VertexConsumer vertexConsumer = GuiRenderStateSink.bufferSource(graphics).getBuffer(baseState().renderType());
        buildVertices(vertexConsumer, 0);
        *///?}
    }


}

