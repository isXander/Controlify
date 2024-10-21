package dev.isxander.controlify.utils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public final class Blit {
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

    public static void blitSprite(
            GuiGraphics graphics,
            TextureAtlasSprite sprite,
            int x, int y,
            int width, int height,
            int color
    ) {
        graphics.blitSprite(
                //? if >=1.21.2
                RenderType::guiTextured,
                sprite,
                x, y,
                width, height,
                color
        );
    }
}
