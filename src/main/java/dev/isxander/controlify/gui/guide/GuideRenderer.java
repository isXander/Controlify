package dev.isxander.controlify.gui.guide;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NonNull;

public final class GuideRenderer {
    private GuideRenderer() {}

    public static void extractRenderState(GuiGraphicsExtractor graphics, GuideDomain<?> domain, Minecraft minecraft, boolean bottomAligned, boolean textContrast) {
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        renderLines(graphics, domain.leftGuides(), minecraft.font, width, height, bottomAligned, false, textContrast);
        renderLines(graphics, domain.rightGuides(), minecraft.font, width, height, bottomAligned, true, textContrast);
    }

    private static void renderLines(GuiGraphicsExtractor graphics, PrecomputedLines lines, Font font, int width, int height, boolean bottomAligned, boolean rightAligned, boolean textContrast) {
        int safeAreaX = 2;
        int safeAreaY = 5;
        int betweenLines = 2;

        int allLinesHeight = lines.height() + (lines.lines().size() - 1) * betweenLines;

        int x = rightAligned ? (width - safeAreaX) : safeAreaX;
        int y = bottomAligned ? (height - allLinesHeight - safeAreaY) : safeAreaY;

        var list = bottomAligned ? Lists.reverse(lines.lines()) : lines.lines();
        for (PrecomputedLines.PrecomputedLine line : list) {
            int lineX = rightAligned ? (x - line.width()) : x;

            if (textContrast) {
                graphics.fill(
                        lineX + line.backgroundLeft() - 1, y - 1,
                        lineX + line.backgroundRight() + 1, y + font.lineHeight + 1, // use font.lineHeight for the height of the line since we're just contrasting the regular text
                        0x80000000
                );
            }

            graphics.text(font, line.text(), lineX, y, 0xFFFFFFFF, !textContrast);

            y += line.height() + betweenLines;
        }
    }

    public static class Renderable implements net.minecraft.client.gui.components.Renderable {
        private final GuideDomain<?> domain;
        private final Minecraft minecraft;
        private boolean bottomAligned;
        private boolean textContrast;

        public Renderable(GuideDomain<?> domain, Minecraft minecraft, boolean bottomAligned, boolean textContrast) {
            this.domain = domain;
            this.minecraft = minecraft;
            this.bottomAligned = bottomAligned;
            this.textContrast = textContrast;
        }


        @Override
        public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            GuideRenderer.extractRenderState(graphics, domain, minecraft, bottomAligned, textContrast);
        }

        public void setBottomAligned(boolean bottomAligned) {
            this.bottomAligned = bottomAligned;
        }

        public void setTextContrast(boolean textContrast) {
            this.textContrast = textContrast;
        }
    }
}
