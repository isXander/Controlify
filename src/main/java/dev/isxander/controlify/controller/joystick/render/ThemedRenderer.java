package dev.isxander.controlify.controller.joystick.render;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.bindings.JoystickAxisBind;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public abstract class ThemedRenderer {
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final String themeId;

    public ThemedRenderer(String themeId) {
        this.themeId = themeId;
    }

    public static class Button extends ThemedRenderer implements JoystickRenderer.Button {
        private final ResourceLocation texture;

        public Button(String themeId, String identifier) {
            super(themeId);
            this.texture = new ResourceLocation("controlify", "textures/gui/joystick/" + themeId + "/" + identifier + ".png");
        }

        @Override
        public DrawSize render(PoseStack poseStack, int x, int centerY, int size, boolean down) {
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            poseStack.pushPose();
            poseStack.translate(x, centerY, 0);

            float scale = (float) size / 22f;
            poseStack.scale(scale, scale, 1);
            poseStack.translate(0f, -DEFAULT_SIZE / scale / 2f, 0);

            GuiComponent.blit(poseStack, 0, 0, 0, 0, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);

            poseStack.popPose();

            return new DrawSize(size, size);
        }
    }

    public static class Axis extends ThemedRenderer implements JoystickRenderer.Axis {
        private final ResourceLocation texture;

        public Axis(String themeId, String identifier) {
            super(themeId);
            this.texture = new ResourceLocation("controlify", "textures/gui/joystick/" + themeId + "/" + identifier + ".png");
        }

        @Override
        public DrawSize render(PoseStack poseStack, int x, int centerY, int size, JoystickAxisBind.AxisDirection direction) {
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            poseStack.pushPose();
            poseStack.translate(x, centerY, 0);

            float scale = (float) size / 22f;
            poseStack.scale(scale, scale, 1);
            poseStack.translate(0f, -DEFAULT_SIZE / scale / 2f, 0);

            GuiComponent.blit(
                    poseStack,
                    0, 0,
                    direction.ordinal() * DEFAULT_SIZE, 0,
                    DEFAULT_SIZE, DEFAULT_SIZE,
                    DEFAULT_SIZE * JoystickAxisBind.AxisDirection.values().length, DEFAULT_SIZE
            );

            poseStack.popPose();

            return new DrawSize(size, size);
        }
    }

    public static class Hat extends ThemedRenderer implements JoystickRenderer.Hat {
        private final ResourceLocation texture;

        public Hat(String themeId, String identifier) {
            super(themeId);
            this.texture = new ResourceLocation("controlify", "textures/gui/joystick/" + themeId + "/" + identifier + ".png");
        }

        @Override
        public DrawSize render(PoseStack poseStack, int x, int centerY, int size, JoystickState.HatState hatState) {
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            poseStack.pushPose();
            poseStack.translate(x, centerY, 0);

            float scale = (float) size / 22f;
            poseStack.scale(scale, scale, 1);
            poseStack.translate(0f, -DEFAULT_SIZE / scale / 2f, 0);

            GuiComponent.blit(
                    poseStack,
                    0, 0,
                    hatState.ordinal() * DEFAULT_SIZE, 0,
                    DEFAULT_SIZE, DEFAULT_SIZE,
                    DEFAULT_SIZE * JoystickState.HatState.values().length, DEFAULT_SIZE
            );

            poseStack.popPose();

            return new DrawSize(size, size);
        }
    }
}
