package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.controlify.utils.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public final class Blit {
    public static void drawSpecial(GuiGraphics graphics, Consumer<MultiBufferSource> consumer) {
        //? if >=1.21.2 {
        graphics.drawSpecial(consumer);
        //?} else {
        /*// noinspection deprecation
        graphics.drawManaged(() -> {
            consumer.accept(graphics.bufferSource());
        });
        *///?}
    }

    public static void blitTex(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x, int y,
            float u, float v,
            int width, int height,
            int textureWidth, int textureHeight
    ) {
        graphics.blit(
                //? if >=1.21.2
                RenderType::guiTextured,
                texture,
                x, y,
                u, v,
                width, height,
                textureWidth, textureHeight
        );
    }

    public static void blitTex(
            GuiGraphics graphics,
            ResourceLocation atlasLocation,
            int x, int y,
            int textureX, int textureY,
            int width, int height,
            int atlasWidth, int atlasHeight
    ) {
        graphics.blit(
                //? if >=1.21.2
                RenderType::guiTextured,
                atlasLocation,
                x, y,
                textureX, textureY,
                width, height,
                atlasWidth, atlasHeight
        );
    }

    public static void blitTex(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x, int y,
            float u, float v,
            int width, int height,
            int textureWidth, int textureHeight,
            int color
    ) {
        //? <1.21.2 {
        /*float[] argb = ColorUtils.decomposeARGBFloat(color);
        graphics.setColor(argb[1], argb[2], argb[3], argb[0]);
        *///?}

        graphics.blit(
                //? if >=1.21.2
                RenderType::guiTextured,
                texture,
                x, y,
                u, v,
                width, height,
                textureWidth, textureHeight,
                //? if >=1.21.2
                color
        );

        //? <1.21.2
        /*graphics.setColor(1, 1, 1, 1);*/
    }

    public static void blitSprite(
            GuiGraphics graphics,
            ResourceLocation sprite,
            int x, int y,
            int width, int height
    ) {
        graphics.blitSprite(
                //? if >=1.21.2
                RenderType::guiTextured,
                sprite,
                x, y,
                width, height
        );
    }

    public static void blitSprite(
            GuiGraphics graphics,
            ResourceLocation sprite,
            int textureWidth, int textureHeight,
            int u, int v,
            int x, int y,
            int width, int height
    ) {
        graphics.blitSprite(
                //? if >=1.21.2
                RenderType::guiTextured,
                sprite,
                textureWidth, textureHeight,
                u, v,
                x, y,
                width, height
        );
    }

    public static void blitSprite(
            GuiGraphics graphics,
            TextureAtlasSprite sprite,
            int x, int y,
            int width, int height,
            int color
    ) {
        //? if >=1.21.2 {
        graphics.blitSprite(
                RenderType::guiTextured,
                sprite,
                x, y,
                width, height,
                color
        );
        //?} else {
        /*float[] argb = ColorUtils.decomposeARGBFloat(color);
        graphics.blit(
                x, y, 0,
                width, height,
                sprite,
                argb[1], argb[2], argb[3], argb[0]
        );
        *///?}
    }

    public static void setPosShader() {
        RenderSystem.setShader(
                //? if >=1.21.2 {
                CoreShaders.POSITION
                //?} else {
                /*GameRenderer::getPositionShader
                *///?}
        );
    }

    public static void setPosColorShader() {
        RenderSystem.setShader(
                //? if >=1.21.2 {
                CoreShaders.POSITION_COLOR
                //?} else {
                /*GameRenderer::getPositionColorShader
                 *///?}
        );
    }

    public static void setPosTexShader() {
        RenderSystem.setShader(
                //? if >=1.21.2 {
                CoreShaders.POSITION_TEX
                //?} else {
                /*GameRenderer::getPositionTexShader
                 *///?}
        );
    }

    public static void setPosTexColorShader() {
        RenderSystem.setShader(
                //? if >=1.21.2 {
                CoreShaders.POSITION_TEX_COLOR
                //?} else {
                /*GameRenderer::getPositionTexColorShader
                 *///?}
        );
    }
}
