package dev.isxander.controlify.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.controlify.utils.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public final class Blit {
    public static void batchDraw(GuiGraphics graphics, Runnable renderer) {
        //? if >=1.21.6 {
        renderer.run();
        //?} elif >=1.21.2 {
        /*graphics.drawSpecial(bufferSource -> renderer.run());
        *///?} else {
        /*// noinspection deprecation
        graphics.drawManaged(renderer);
        *///?}
    }

    public static void tex(
            GuiGraphics graphics,
            Identifier texture,
            int x, int y,
            float u, float v,
            int width, int height,
            int textureWidth, int textureHeight
    ) {
        //? if <1.21.2
        //RenderSystem.enableBlend(); // maintain parity with guiTextured render type that enables blending

        graphics.blit(
                //? if >=1.21.2
                guiTextured(),
                texture,
                x, y,
                u, v,
                width, height,
                textureWidth, textureHeight
        );

        //? if <1.21.2
        //RenderSystem.disableBlend();
    }

    public static void tex(
            GuiGraphics graphics,
            Identifier atlasLocation,
            int x, int y,
            int textureX, int textureY,
            int width, int height,
            int atlasWidth, int atlasHeight
    ) {
        //? if <1.21.2
        //RenderSystem.enableBlend(); // maintain parity with guiTextured render type that enables blending

        graphics.blit(
                //? if >=1.21.2
                guiTextured(),
                atlasLocation,
                x, y,
                textureX, textureY,
                width, height,
                atlasWidth, atlasHeight
        );

        //? if <1.21.2
        //RenderSystem.disableBlend();
    }

    public static void tex(
            GuiGraphics graphics,
            Identifier texture,
            int x, int y,
            float u, float v,
            int width, int height,
            int textureWidth, int textureHeight,
            int color
    ) {
        //? if <1.21.2 {
        /*float[] argb = ColorUtils.decomposeARGBFloat(color);
        graphics.setColor(argb[1], argb[2], argb[3], argb[0]);

        RenderSystem.enableBlend(); // maintain parity with guiTextured render type that enables blending
        *///?}

        graphics.blit(
                //? if >=1.21.2
                guiTextured(),
                texture,
                x, y,
                u, v,
                width, height,
                textureWidth, textureHeight
                //? if >=1.21.2
                ,color
        );

        //? if <1.21.2 {
        /*RenderSystem.disableBlend();

        graphics.setColor(1, 1, 1, 1);
        *///?}
    }

    public static void sprite(
            GuiGraphics graphics,
            Identifier sprite,
            int x, int y,
            int width, int height
    ) {
        graphics.blitSprite(
                //? if >=1.21.2
                guiTextured(),
                sprite,
                x, y,
                width, height
        );
    }

    public static void sprite(
            GuiGraphics graphics,
            Identifier sprite,
            int textureWidth, int textureHeight,
            int u, int v,
            int x, int y,
            int width, int height
    ) {
        graphics.blitSprite(
                //? if >=1.21.2
                guiTextured(),
                sprite,
                textureWidth, textureHeight,
                u, v,
                x, y,
                width, height
        );
    }

    public static void sprite(
            GuiGraphics graphics,
            TextureAtlasSprite sprite,
            int x, int y,
            int width, int height,
            int color
    ) {
        //? if >=1.21.2 {
        graphics.blitSprite(
                guiTextured(),
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

    //? if >=1.21.6 {
    public static com.mojang.blaze3d.pipeline.RenderPipeline guiTextured() {
        return RenderPipelines.GUI_TEXTURED;
    }
    //?} elif >=1.21.2 {
    /*public static Function<Identifier, RenderType> guiTextured() {
        return RenderType::guiTextured;
    }
    *///?}
}
