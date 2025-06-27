package dev.isxander.controlify.utils.render.elements;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.isxander.controlify.utils.render.BaseRenderState;
import dev.isxander.controlify.utils.render.CGuiElementRenderState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public record CircleElementRenderState(
        BaseRenderState baseState,
        float originX, float originY,
        float radius, float thickness,
        int color,
        int segments
) implements CGuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertexConsumer, float z) {
        float innerRadius = radius - thickness;
        for (int i = 0; i < segments; i++) {
            int j = (i + 1) % segments;
            float angle     = (float) i / segments * Mth.TWO_PI;
            float nextAngle = (float) j / segments * Mth.TWO_PI;

            float xi1 = originX + Mth.sin(angle)     * innerRadius;
            float yi1 = originY + Mth.cos(angle)     * innerRadius;
            float xi2 = originX + Mth.sin(nextAngle) * innerRadius;
            float yi2 = originY + Mth.cos(nextAngle) * innerRadius;

            float xo1 = originX + Mth.sin(angle)     * radius;
            float yo1 = originY + Mth.cos(angle)     * radius;
            float xo2 = originX + Mth.sin(nextAngle) * radius;
            float yo2 = originY + Mth.cos(nextAngle) * radius;

            add2DVertex(vertexConsumer, xi1, yi1, z).setColor(color);
            add2DVertex(vertexConsumer, xo1, yo1, z).setColor(color);
            add2DVertex(vertexConsumer, xo2, yo2, z).setColor(color);
            add2DVertex(vertexConsumer, xi2, yi2, z).setColor(color);
        }
    }

    public static CircleElementRenderState outline(
            GuiGraphics graphics,
            float originX, float originY,
            float radius, float thickness,
            int color
    ) {
        int minX = Mth.floor(originX - radius);
        int minY = Mth.floor(originY - radius);
        int maxX = Mth.ceil(originX + radius);
        int maxY = Mth.ceil(originY + radius);

        return new CircleElementRenderState(
                BaseRenderState.create(graphics, null, minX, minY, maxX, maxY),
                originX, originY,
                radius, thickness,
                color, segmentsForRadius(radius)
        );
    }

    public static CircleElementRenderState filled(
            GuiGraphics graphics,
            float originX, float originY,
            float radius, int color
    ) {
        return outline(graphics, originX, originY, radius, radius, color);
    }

    private static int segmentsForRadius(float radius) {
        // 2 * PI * r / 4
        // each segment is about 4 pixels long
        return Mth.ceil(2 * Math.PI * radius / 4);
    }
}
