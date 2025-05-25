package dev.isxander.controlify.gui.controllers;

import dev.isxander.controlify.controller.input.DeadzoneGroup;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.utils.render.elements.CircleElementRenderState;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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
        CircleElementRenderState.outline(
                graphics,
                x + radius, y + radius,
                radius, 1f,
                -1
        ).submit(graphics);
        // deadzone outline
        float deadzone = deadzoneOption.get().pendingValue();
        boolean aboveDeadzone = Math.abs(currentX) > deadzone || Math.abs(currentY) > deadzone;
        CircleElementRenderState.outline(
                graphics,
                x + radius, y + radius,
                deadzone * radius, 1f,
                aboveDeadzone ? 0xFF00FFFF : 0xFFFF0000
        ).submit(graphics);

        // current axis point
        CircleElementRenderState.filled(
                graphics,
                x + radius + currentX * radius,
                y + radius + currentY * radius,
                1f, 0xFF00FF00
        ).submit(graphics);

        Font font = Minecraft.getInstance().font;
        DecimalFormat format = new DecimalFormat("0.000");
        graphics.drawString(font, "X: " + format.format(currentX), (int) (x + radius * 2 + 5), y, -1);
        graphics.drawString(font, "Y: " + format.format(currentY), (int) (x + radius * 2 + 5), y + font.lineHeight + 1, -1);

        return renderHeight;
    }

    @Override
    public void close() {

    }
}
