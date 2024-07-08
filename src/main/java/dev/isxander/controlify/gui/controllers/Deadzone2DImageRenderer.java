package dev.isxander.controlify.gui.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.isxander.controlify.controller.input.DeadzoneGroup;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.render.ControlifyVertexConsumer;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Supplier;

public class Deadzone2DImageRenderer implements ImageRenderer {
    private final InputComponent input;
    private final DeadzoneGroup deadzoneGroup;
    private final Supplier<Option<Float>> deadzoneOption;

    public Deadzone2DImageRenderer(InputComponent input, DeadzoneGroup deadzoneGroup, Supplier<Option<Float>> deadzoneOption) {
        this.input = input;
        this.deadzoneGroup = deadzoneGroup;
        this.deadzoneOption = deadzoneOption;
    }

    @Override
    public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
        float radius = renderWidth / 4f;
        int renderHeight = (int) (radius * 2f);

        // axes go up -> down -> left -> right
        List<ResourceLocation> deadzones = deadzoneGroup.axes();
        float up = input.rawStateNow().getAxisState(deadzones.get(0));
        float down = input.rawStateNow().getAxisState(deadzones.get(1));
        float left = input.rawStateNow().getAxisState(deadzones.get(2));
        float right = input.rawStateNow().getAxisState(deadzones.get(3));
        float currentX = right - left;
        float currentY = down - up;

        // axis lines
        graphics.hLine(x, (int)(x + radius*2), (int) (y + radius), 0xFFAAAAAA);
        graphics.vLine((int) (x + radius), y, (int)(y + radius*2), 0xFFAAAAAA);

        // 100% outline
        drawCircleOutline(graphics.pose(), x + radius, y + radius, 0, radius, 1f, -1, 360);

        // deadzone outline
        float deadzone = deadzoneOption.get().pendingValue();
        boolean aboveDeadzone = Math.abs(currentX) > deadzone || Math.abs(currentY) > deadzone;
        drawCircleOutline(graphics.pose(), x + radius, y + radius, 0, deadzone * radius, 1f, aboveDeadzone ? 0xFF00FFFF : 0xFFFF0000, 360);

        // current axis point
        drawCircle(graphics.pose(), x + radius + currentX * radius, y + radius + currentY * radius, 0, 1f, 0xFF00FF00, 360);

        Font font = Minecraft.getInstance().font;
        DecimalFormat format = new DecimalFormat("0.000");
        graphics.drawString(font, "X: " + format.format(currentX), (int) (x + radius * 2 + 5), y, -1);
        graphics.drawString(font, "Y: " + format.format(currentY), (int) (x + radius * 2 + 5), y + font.lineHeight + 1, -1);

        return renderHeight;
    }

    @Override
    public void close() {

    }

    private static void drawCircle(PoseStack poseStack, float originX, float originY, float z, float radius, int colour, int segments) {
        BufferBuilder buffer = CUtil.beginBuffer(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f position = poseStack.last().pose();

        for (int i = 0; i < 360; i += (int) Math.min(360.0 / segments, 360 - i)) {
            float radians = i * Mth.DEG_TO_RAD;
            float x = originX + Mth.sin(radians) * radius;
            float y = originY + Mth.cos(radians) * radius;

            /*? if >1.20.6 {*/
            buffer.addVertex(position, x, y, z).setColor(colour);
            /*?} else {*/
            /*buffer.vertex(position, x, y, z).color(colour).endVertex();
            *//*?}*/
        }

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferUploader.drawWithShader(
                /*? if >1.20.6 {*/
                buffer.buildOrThrow()
                /*?} else {*/
                /*buffer.end()
                *//*?}*/
        );

        RenderSystem.disableBlend();
    }

    private static void drawCircleOutline(PoseStack poseStack, float originX, float originY, float z, float radius, float thickness, int colour, int segments) {
        BufferBuilder buffer = CUtil.beginBuffer(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        ControlifyVertexConsumer vertexConsumer = ControlifyVertexConsumer.of(buffer);
        Matrix4f position = poseStack.last().pose();

        float diff = radius - thickness;
        for (int i = 0; i <= segments; i++) {
            float radians = ((float) i / segments * 360f) * Mth.DEG_TO_RAD;
            float sin = Mth.sin(radians);
            float cos = Mth.cos(radians);

            float x1 = originX + sin * diff;
            float y1 = originY + cos * diff;

            vertexConsumer.vertex(position, x1, y1, z).color(colour);

            float x2 = originX + sin * radius;
            float y2 = originY + cos * radius;

            vertexConsumer.vertex(position, x2, y2, z).color(colour);
        }

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferUploader.drawWithShader(
                /*? if >1.20.6 {*/
                buffer.buildOrThrow()
                /*?} else {*/
                /*buffer.end()
                 *//*?}*/
        );

        RenderSystem.disableBlend();
    }
}
