package dev.isxander.controlify.utils.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class RenderUtils {
    private static final Identifier GREEN_BACK_BAR = Identifier.withDefaultNamespace("boss_bar/green_background");
    private static final Identifier GREEN_FRONT_BAR = Identifier.withDefaultNamespace("boss_bar/green_progress");

    private RenderUtils() {
    }

    public static void extractBar(GuiGraphicsExtractor graphics, int centerX, int y, float progress) {
        int width = Mth.lerpDiscrete(progress, 0, 182);

        int x = centerX - 182 / 2;

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                GREEN_BACK_BAR,
                182, 5,
                0, 0,
                x, y,
                182, 5
        );
        if (width > 0) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    GREEN_FRONT_BAR,
                    182, 5,
                    0, 0,
                    x, y,
                    width, 5
            );
        }
    }
}
