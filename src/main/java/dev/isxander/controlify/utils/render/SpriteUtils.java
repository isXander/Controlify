package dev.isxander.controlify.utils.render;

import dev.isxander.controlify.mixins.core.GuiGraphicsAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class SpriteUtils {
    public static void blitSprite(GuiGraphics graphics, ControlifySprite sprite, int x, int y, int width, int height) {
        if (sprite.scaling() instanceof SpriteScaling.Stretch) {
            sprite(graphics, sprite, x, y, width, height);
        } else if (sprite.scaling() instanceof SpriteScaling.Tiled tile) {
            tiledSprite(graphics, sprite, x, y, width, height, 0, 0, tile.width(), tile.height(), tile.width(), tile.height());
        } else if (sprite.scaling() instanceof SpriteScaling.NineSlice nineSlice) {
            nineSlicedSprite(graphics, sprite, nineSlice, x, y, width, height);
        }
    }

    public static void rect(GuiGraphics graphics, ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, float minU, float maxU, float minV, float maxV) {
        ControlifyVertexConsumer vertexConsumer = ControlifyVertexConsumer.of(
                graphics.bufferSource().getBuffer(ExtraRenderTypes.BLIT_TEXTURE.apply(atlasLocation))
        );
        Matrix4f pose = graphics.pose().last().pose();

        vertexConsumer.vertex(pose, x1, y1, 0).uv(minU, minV).endVertex();
        vertexConsumer.vertex(pose, x1, y2, 0).uv(minU, maxV).endVertex();
        vertexConsumer.vertex(pose, x2, y2, 0).uv(maxU, maxV).endVertex();
        vertexConsumer.vertex(pose, x2, y1, 0).uv(maxU, minV).endVertex();

        ((GuiGraphicsAccessor) graphics).invokeFlushIfUnmanaged();
    }

    public static void sprite(GuiGraphics graphics, ControlifySprite sprite, int x, int y, int width, int height) {
        rect(graphics, sprite.atlas(), x, x + width, y, y + height, sprite.u0(), sprite.u1(), sprite.v0(), sprite.v1());
    }

    public static void sprite(GuiGraphics graphics, ControlifySprite sprite, int textureWidth, int textureHeight, int uPosition, int vPosition, int x, int y, int uWidth, int vHeight) {
        rect(
                graphics,
                sprite.atlas(),
                x, x + uWidth,
                y, y + vHeight,
                sprite.getU((float) uPosition / textureWidth),
                sprite.getU((float) (uPosition + uWidth) / textureWidth),
                sprite.getV((float) vPosition / textureHeight),
                sprite.getV((float) (vPosition + vHeight) / textureHeight)
        );
    }

    public static void tiledSprite(GuiGraphics graphics, ControlifySprite sprite, int x, int y, int width, int height, int u, int v, int spriteWidth, int spriteHeight, int nineSliceWidth, int nineSliceHeight) {
        if (width > 0 && height > 0) {
            if (spriteWidth > 0 && spriteHeight > 0) {
                for(int i = 0; i < width; i += spriteWidth) {
                    int uWidth = Math.min(spriteWidth, width - i);

                    for(int k = 0; k < height; k += spriteHeight) {
                        int vHeight = Math.min(spriteHeight, height - k);
                        sprite(graphics, sprite, nineSliceWidth, nineSliceHeight, u, v, x + i, y + k, uWidth, vHeight);
                    }
                }

            } else {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + spriteWidth + "x" + spriteHeight);
            }
        }
    }

    public static void nineSlicedSprite(GuiGraphics graphics, ControlifySprite sprite, SpriteScaling.NineSlice nineSlice, int x, int y, int width, int height) {
        SpriteScaling.NineSlice.Border border = nineSlice.border();
        int i = Math.min(border.left(), width / 2);
        int j = Math.min(border.right(), width / 2);
        int k = Math.min(border.top(), height / 2);
        int l = Math.min(border.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, height);
        } else if (height == nineSlice.height()) {
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, i, height);
            tiledSprite(graphics, sprite, x + i, y, width - j - i, height, i, 0, nineSlice.width() - j - i, nineSlice.height(), nineSlice.width(), nineSlice.height());
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, j, height);
        } else if (width == nineSlice.width()) {
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, k);
            tiledSprite(graphics, sprite, x, y + k, width, height - l - k, 0, k, nineSlice.width(), nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, width, l);
        } else {
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, i, k);
            tiledSprite(graphics, sprite, x + i, y, width - j - i, k, i, 0, nineSlice.width() - j - i, k, nineSlice.width(), nineSlice.height());
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, j, k);
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, i, l);
            tiledSprite(graphics, sprite, x + i, y + height - l, width - j - i, l, i, nineSlice.height() - l, nineSlice.width() - j - i, l, nineSlice.width(), nineSlice.height());
            sprite(graphics, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, nineSlice.height() - l, x + width - j, y + height - l, j, l);
            tiledSprite(graphics, sprite, x, y + k, i, height - l - k, 0, k, i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            tiledSprite(graphics, sprite, x + i, y + k, width - j - i, height - l - k, i, k, nineSlice.width() - j - i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            tiledSprite(graphics, sprite, x + width - j, y + k, i, height - l - k, nineSlice.width() - j, k, j, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
        }
    }
}
