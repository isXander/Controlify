package dev.isxander.controlify.utils.render;

import dev.isxander.yacl3.gui.utils.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

//? if >=1.21.6 {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
*///?} else {
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
//?}

public record BaseRenderState(
        //? if >=1.21.6 {
        /*RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        @Nullable ScreenRectangle bounds,
        @Nullable ScreenRectangle scissorArea
        *///?} else {
        RenderType renderType,
        Matrix4f pose
        //?}
) {
    public static BaseRenderState create(GuiGraphics graphics, @Nullable ResourceLocation texture, int x0, int y0, int x1, int y1) {
        //? if >=1.21.6 {
        /*@Nullable ScreenRectangle scissorArea = GuiRenderStateSink.peekScissorStack(graphics);
        ScreenRectangle bounds = boundsFromMaxPoints(x0, y0, x1, y1, graphics.pose(), scissorArea);

        return new BaseRenderState(
                texture != null ? RenderPipelines.GUI_TEXTURED : RenderPipelines.GUI,
                textureSetup(texture),
                new Matrix3x2f(graphics.pose()),
                bounds, scissorArea
        );
        *///?} else {
        return create(graphics, texture);
        //?}
    }

    public static BaseRenderState create(GuiGraphics graphics, @Nullable ResourceLocation texture) {
        //? if >=1.21.6 {
        /*return new BaseRenderState(
                texture != null ? RenderPipelines.GUI_TEXTURED : RenderPipelines.GUI,
                textureSetup(texture),
                new Matrix3x2f(graphics.pose()),
                null, GuiRenderStateSink.peekScissorStack(graphics)
        );
        *///?} else {
        return new BaseRenderState(
                texture != null ? GuiUtils.guiTextured(false).apply(texture) : RenderType.gui(),
                graphics.pose().last().pose()
        );
        //?}
    }

    //? if >=1.21.6 {
    /*private static TextureSetup textureSetup(@Nullable ResourceLocation texture) {
        return texture == null
                ? TextureSetup.noTexture()
                : TextureSetup.singleTexture(Minecraft.getInstance().getTextureManager().getTexture(texture).getTextureView());
    }

    private static ScreenRectangle boundsFromMaxPoints(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
    *///?}
}
