package dev.isxander.controlify.utils;

import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A compatibility class for rendering sprites pre 1.20.3 where sprites didn't exist
 */
public final class FakeSpriteRenderer {
    public static void blitNineSlicedSprite(
            GuiGraphics graphics,
            ResourceLocation atlasLocation,
            int x, int y,
            int width, int height,
            int sliceSize,
            int uOffset, int vOffset
    ) {
        blitNineSliced(graphics, atlasLocation, x, y, width, height, sliceSize, sliceSize, sliceSize, sliceSize, uOffset, vOffset, uOffset, vOffset, uOffset, vOffset);
    }

    public static void blitNineSliced(
            GuiGraphics graphics,
            ResourceLocation atlasLocation,
            int x,
            int y,
            int width,
            int height,
            int leftSliceWidth,
            int topSliceHeight,
            int rightSliceWidth,
            int bottomSliceHeight,
            int uWidth,
            int vHeight,
            int textureX,
            int textureY,
            int atlasWidth,
            int atlasHeight
    ) {
        leftSliceWidth = Math.min(leftSliceWidth, width / 2);
        rightSliceWidth = Math.min(rightSliceWidth, width / 2);
        topSliceHeight = Math.min(topSliceHeight, height / 2);
        bottomSliceHeight = Math.min(bottomSliceHeight, height / 2);
        if (width == uWidth && height == vHeight) {
            graphics.blit(atlasLocation, x, y, textureX, textureY, width, height, atlasWidth, atlasHeight);
        } else if (height == vHeight) {
            graphics.blit(atlasLocation, x, y, textureX, textureY, leftSliceWidth, height, atlasWidth, atlasHeight);
            blitRepeating(
                    graphics,
                    atlasLocation,
                    x + leftSliceWidth,
                    y,
                    width - rightSliceWidth - leftSliceWidth,
                    height,
                    textureX + leftSliceWidth,
                    textureY,
                    uWidth - rightSliceWidth - leftSliceWidth,
                    vHeight,
                    atlasWidth,
                    atlasHeight
            );
            graphics.blit(atlasLocation, x + width - rightSliceWidth, y, textureX + uWidth - rightSliceWidth, textureY, rightSliceWidth, height, atlasWidth, atlasHeight);
        } else if (width == uWidth) {
            graphics.blit(atlasLocation, x, y, textureX, textureY, width, topSliceHeight,atlasWidth, atlasHeight);
            blitRepeating(
                    graphics,
                    atlasLocation,
                    x,
                    y + topSliceHeight,
                    width,
                    height - bottomSliceHeight - topSliceHeight,
                    textureX,
                    textureY + topSliceHeight,
                    uWidth,
                    vHeight - bottomSliceHeight - topSliceHeight,
                    atlasWidth,
                    atlasHeight
            );
            graphics.blit(atlasLocation, x, y + height - bottomSliceHeight, textureX, textureY + vHeight - bottomSliceHeight, width, bottomSliceHeight, atlasWidth, atlasHeight);
        } else {
            graphics.blit(atlasLocation, x, y, textureX, textureY, leftSliceWidth, topSliceHeight, atlasWidth, atlasHeight);
            blitRepeating(
                    graphics,
                    atlasLocation,
                    x + leftSliceWidth,
                    y,
                    width - rightSliceWidth - leftSliceWidth,
                    topSliceHeight,
                    textureX + leftSliceWidth,
                    textureY,
                    uWidth - rightSliceWidth - leftSliceWidth,
                    topSliceHeight,
                    atlasWidth,
                    atlasHeight
            );
            graphics.blit(atlasLocation, x + width - rightSliceWidth, y, textureX + uWidth - rightSliceWidth, textureY, rightSliceWidth, topSliceHeight, atlasWidth, atlasHeight);
            graphics.blit(atlasLocation, x, y + height - bottomSliceHeight, textureX, textureY + vHeight - bottomSliceHeight, leftSliceWidth, bottomSliceHeight, atlasWidth, atlasHeight);
            blitRepeating(
                    graphics,
                    atlasLocation,
                    x + leftSliceWidth,
                    y + height - bottomSliceHeight,
                    width - rightSliceWidth - leftSliceWidth,
                    bottomSliceHeight,
                    textureX + leftSliceWidth,
                    textureY + vHeight - bottomSliceHeight,
                    uWidth - rightSliceWidth - leftSliceWidth,
                    bottomSliceHeight,
                    atlasWidth,
                    atlasHeight
            );
            graphics.blit(
                    atlasLocation,
                    x + width - rightSliceWidth,
                    y + height - bottomSliceHeight,
                    textureX + uWidth - rightSliceWidth,
                    textureY + vHeight - bottomSliceHeight,
                    rightSliceWidth,
                    bottomSliceHeight,
                    atlasWidth,
                    atlasHeight
            );
            blitRepeating(
                    graphics,
                    atlasLocation,
                    x,
                    y + topSliceHeight,
                    leftSliceWidth,
                    height - bottomSliceHeight - topSliceHeight,
                    textureX,
                    textureY + topSliceHeight,
                    leftSliceWidth,
                    vHeight - bottomSliceHeight - topSliceHeight,
                    atlasWidth,
                    atlasHeight
            );
            blitRepeating(
                    graphics,
                    atlasLocation,
                    x + leftSliceWidth,
                    y + topSliceHeight,
                    width - rightSliceWidth - leftSliceWidth,
                    height - bottomSliceHeight - topSliceHeight,
                    textureX + leftSliceWidth,
                    textureY + topSliceHeight,
                    uWidth - rightSliceWidth - leftSliceWidth,
                    vHeight - bottomSliceHeight - topSliceHeight,
                    atlasWidth,
                    atlasHeight
            );
            blitRepeating(
                    graphics,
                    atlasLocation,
                    x + width - rightSliceWidth,
                    y + topSliceHeight,
                    leftSliceWidth,
                    height - bottomSliceHeight - topSliceHeight,
                    textureX + uWidth - rightSliceWidth,
                    textureY + topSliceHeight,
                    rightSliceWidth,
                    vHeight - bottomSliceHeight - topSliceHeight,
                    atlasWidth,
                    atlasHeight
            );
        }
    }

    public static void blitRepeating(GuiGraphics graphics, ResourceLocation atlasLocation, int x, int y, int width, int height, int uOffset, int vOffset, int sourceWidth, int sourceHeight, int atlasWidth, int atlasHeight) {
        int i = x;

        int j;
        for(IntIterator intIterator = slices(width, sourceWidth); intIterator.hasNext(); i += j) {
            j = intIterator.nextInt();
            int k = (sourceWidth - j) / 2;
            int l = y;

            int m;
            for(IntIterator intIterator2 = slices(height, sourceHeight); intIterator2.hasNext(); l += m) {
                m = intIterator2.nextInt();
                int n = (sourceHeight - m) / 2;
                graphics.blit(atlasLocation, i, l, uOffset + k, vOffset + n, j, m, atlasWidth, atlasHeight);
            }
        }
    }

    private static IntIterator slices(int target, int total) {
        int i = Mth.positiveCeilDiv(target, total);
        return new Divisor(target, i);
    }

    private FakeSpriteRenderer() {
    }
}
