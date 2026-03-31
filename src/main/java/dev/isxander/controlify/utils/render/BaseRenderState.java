package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record BaseRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        @Nullable ScreenRectangle bounds,
        @Nullable ScreenRectangle scissorArea
) {
    public static BaseRenderState create(GuiGraphicsExtractor graphics, @Nullable Identifier texture, int x0, int y0, int x1, int y1) {
        //? if fabric
        @Nullable ScreenRectangle scissorArea = graphics.scissorStack.peek();
        //? if neoforge
        //@Nullable ScreenRectangle scissorArea = graphics.peekScissorStack();

        ScreenRectangle bounds = boundsFromMaxPoints(x0, y0, x1, y1, graphics.pose(), scissorArea);

        return new BaseRenderState(
                texture != null ? RenderPipelines.GUI_TEXTURED : RenderPipelines.GUI,
                textureSetup(texture),
                new Matrix3x2f(graphics.pose()),
                bounds, scissorArea
        );
    }

    public static BaseRenderState create(GuiGraphicsExtractor graphics, @Nullable Identifier texture) {
        //? if fabric
        @Nullable ScreenRectangle scissorArea = graphics.scissorStack.peek();
        //? if neoforge
        //@Nullable ScreenRectangle scissorArea = graphics.peekScissorStack();

        return new BaseRenderState(
                texture != null ? RenderPipelines.GUI_TEXTURED : RenderPipelines.GUI,
                textureSetup(texture),
                new Matrix3x2f(graphics.pose()),
                null, scissorArea
        );
    }

    private static TextureSetup textureSetup(@Nullable Identifier textureId) {
        if (textureId != null) {
            var texture = Minecraft.getInstance().getTextureManager().getTexture(textureId);
            return TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
        }
        return TextureSetup.noTexture();
    }

    private static ScreenRectangle boundsFromMaxPoints(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
