package dev.isxander.controlify.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.bindings.Bind;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.gui.GuiComponent;

public class ButtonRenderer {
    public static final int BUTTON_SIZE = 22;

    public static void drawButton(Bind button, Controller controller, PoseStack poseStack, int x, int centerY) {
        RenderSystem.setShaderTexture(0, button.textureLocation(controller));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiComponent.blit(poseStack, x, centerY - BUTTON_SIZE / 2, 0, 0, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
    }

    public record DrawSize(int width, int height) { }
}
