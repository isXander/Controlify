package dev.isxander.controlify.utils.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class SpriteUtils {
    public static void blitSprite(GuiGraphics graphics, ControlifySprite sprite, int x, int y, int width, int height) {
        Blit.drawManaged(graphics, multiBufferSource -> {
            blitSprite(multiBufferSource, graphics.pose().last().pose(), sprite, x, y, width, height);
        });
    }

    public static void blitSprite(MultiBufferSource buffer, Matrix4f pose, ControlifySprite sprite, int x, int y, int width, int height) {
        ControlifyVertexConsumer vertexConsumer = ControlifyVertexConsumer.of(
                buffer.getBuffer(ExtraRenderTypes.guiTextured(sprite.atlas()))
        );

        blitSprite(vertexConsumer, pose, sprite, x, y, width, height);
    }

    public static void blitSprite(ControlifyVertexConsumer vertexConsumer, Matrix4f pose, ControlifySprite sprite, int x, int y, int width, int height) {
        if (sprite.scaling() instanceof SpriteScaling.Stretch) {
            sprite(vertexConsumer, pose, sprite, x, y, width, height);
        } else if (sprite.scaling() instanceof SpriteScaling.Tiled tile) {
            tiledSprite(vertexConsumer, pose, sprite, x, y, width, height, 0, 0, tile.width(), tile.height(), tile.width(), tile.height());
        } else if (sprite.scaling() instanceof SpriteScaling.NineSlice nineSlice) {
            nineSlicedSprite(vertexConsumer, pose, sprite, nineSlice, x, y, width, height);
        }
    }

    public static void quad(ControlifyVertexConsumer vertexConsumer, Matrix4f pose, int x1, int x2, int y1, int y2, float minU, float maxU, float minV, float maxV) {
        vertexConsumer.vertex(pose, x1, y1, 0).uv(minU, minV).color(-1).endVertex();
        vertexConsumer.vertex(pose, x1, y2, 0).uv(minU, maxV).color(-1).endVertex();
        vertexConsumer.vertex(pose, x2, y2, 0).uv(maxU, maxV).color(-1).endVertex();
        vertexConsumer.vertex(pose, x2, y1, 0).uv(maxU, minV).color(-1).endVertex();
    }

    public static void sprite(ControlifyVertexConsumer vertexConsumer, Matrix4f pose, ControlifySprite sprite, int x, int y, int width, int height) {
        quad(vertexConsumer, pose, x, x + width, y, y + height, sprite.u0(), sprite.u1(), sprite.v0(), sprite.v1());
    }

    public static void sprite(ControlifyVertexConsumer vertexConsumer, Matrix4f pose, ControlifySprite sprite, int textureWidth, int textureHeight, int uPosition, int vPosition, int x, int y, int uWidth, int vHeight) {
        quad(
                vertexConsumer,
                pose,
                x, x + uWidth,
                y, y + vHeight,
                sprite.getU((float) uPosition / textureWidth),
                sprite.getU((float) (uPosition + uWidth) / textureWidth),
                sprite.getV((float) vPosition / textureHeight),
                sprite.getV((float) (vPosition + vHeight) / textureHeight)
        );
    }

    public static void tiledSprite(ControlifyVertexConsumer vertexConsumer, Matrix4f pose, ControlifySprite sprite, int x, int y, int width, int height, int u, int v, int spriteWidth, int spriteHeight, int nineSliceWidth, int nineSliceHeight) {
        if (width > 0 && height > 0) {
            if (spriteWidth > 0 && spriteHeight > 0) {
                for(int i = 0; i < width; i += spriteWidth) {
                    int uWidth = Math.min(spriteWidth, width - i);

                    for(int k = 0; k < height; k += spriteHeight) {
                        int vHeight = Math.min(spriteHeight, height - k);
                        sprite(vertexConsumer, pose, sprite, nineSliceWidth, nineSliceHeight, u, v, x + i, y + k, uWidth, vHeight);
                    }
                }

            } else {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + spriteWidth + "x" + spriteHeight);
            }
        }
    }

    public static void nineSlicedSprite(ControlifyVertexConsumer vertexConsumer, Matrix4f pose, ControlifySprite sprite, SpriteScaling.NineSlice nineSlice, int x, int y, int width, int height) {
        SpriteScaling.NineSlice.Border border = nineSlice.border();
        int i = Math.min(border.left(), width / 2);
        int j = Math.min(border.right(), width / 2);
        int k = Math.min(border.top(), height / 2);
        int l = Math.min(border.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, height);
        } else if (height == nineSlice.height()) {
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, i, height);
            tiledSprite(vertexConsumer, pose, sprite, x + i, y, width - j - i, height, i, 0, nineSlice.width() - j - i, nineSlice.height(), nineSlice.width(), nineSlice.height());
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, j, height);
        } else if (width == nineSlice.width()) {
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, k);
            tiledSprite(vertexConsumer, pose, sprite, x, y + k, width, height - l - k, 0, k, nineSlice.width(), nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, width, l);
        } else {
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, i, k);
            tiledSprite(vertexConsumer, pose, sprite, x + i, y, width - j - i, k, i, 0, nineSlice.width() - j - i, k, nineSlice.width(), nineSlice.height());
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, j, k);
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, i, l);
            tiledSprite(vertexConsumer, pose, sprite, x + i, y + height - l, width - j - i, l, i, nineSlice.height() - l, nineSlice.width() - j - i, l, nineSlice.width(), nineSlice.height());
            sprite(vertexConsumer, pose, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, nineSlice.height() - l, x + width - j, y + height - l, j, l);
            tiledSprite(vertexConsumer, pose, sprite, x, y + k, i, height - l - k, 0, k, i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            tiledSprite(vertexConsumer, pose, sprite, x + i, y + k, width - j - i, height - l - k, i, k, nineSlice.width() - j - i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
            tiledSprite(vertexConsumer, pose, sprite, x + width - j, y + k, i, height - l - k, nineSlice.width() - j, k, j, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height());
        }
    }
}
