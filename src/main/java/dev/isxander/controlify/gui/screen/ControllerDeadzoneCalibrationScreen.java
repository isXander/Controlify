package dev.isxander.controlify.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ControllerDeadzoneCalibrationScreen extends Screen {
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");

    protected final Controller<?, ?> controller;
    private final Screen parent;

    private MultiLineLabel waitLabel, infoLabel, completeLabel;

    protected Button readyButton;

    protected boolean calibrating = false, calibrated = false;
    protected int calibrationTicks = 0;

    public ControllerDeadzoneCalibrationScreen(Controller<?, ?> controller, Screen parent) {
        super(Component.translatable("controlify.calibration.title"));
        this.controller = controller;
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(
                readyButton = Button.builder(Component.translatable("controlify.calibration.ready"), btn -> {
                            if (!calibrated)
                                startCalibration();
                            else
                                onClose();
                        })
                        .width(150)
                        .pos(this.width / 2 - 75, this.height - 8 - 20)
                        .build());

        this.infoLabel = MultiLineLabel.create(font, Component.translatable("controlify.calibration.info"), width - 30);
        this.waitLabel = MultiLineLabel.create(font, Component.translatable("controlify.calibration.wait"), width - 30);
        this.completeLabel = MultiLineLabel.create(font, Component.translatable("controlify.calibration.complete"), width - 30);
    }

    protected void startCalibration() {
        calibrating = true;

        readyButton.active = false;
        readyButton.setMessage(Component.translatable("controlify.calibration.calibrating"));
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        super.render(matrices, mouseX, mouseY, delta);

        drawCenteredString(matrices, font, Component.translatable("controlify.calibration.title", controller.name()).withStyle(ChatFormatting.BOLD), width / 2, 8, -1);

        RenderSystem.setShaderTexture(0, GUI_BARS_LOCATION);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrices.pushPose();
        matrices.scale(2f, 2f, 1f);
        drawBar(matrices, width / 2 / 2, 30 / 2, 1f, 0);
        var progress = (calibrationTicks - 1 + delta) / 100f;
        if (progress > 0)
            drawBar(matrices, width / 2 / 2, 30 / 2, progress, 5);
        matrices.popPose();

        MultiLineLabel label;
        if (calibrating) label = waitLabel;
        else if (calibrated) label = completeLabel;
        else label = infoLabel;

        label.renderCentered(matrices, width / 2, 55);
    }

    private void drawBar(PoseStack matrices, int centerX, int y, float progress, int vOffset) {
        progress = 1 - (float)Math.pow(1 - progress, 3);

        int x = centerX - 182 / 2;
        this.blit(matrices, x, y, 0, 30 + vOffset, (int)(progress * 182), 5);
    }

    @Override
    public void tick() {
        if (!calibrating)
            return;

        if (stateChanged())
            calibrationTicks = 0;

        if (calibrationTicks < 100) {
            calibrationTicks++;
        } else {
            useCurrentStateAsDeadzone();
            calibrating = false;
            calibrated = true;
            readyButton.active = true;
            readyButton.setMessage(Component.translatable("controlify.calibration.done"));

            controller.config().calibrated = true;
            Controlify.instance().config().save();
        }
    }

    private void useCurrentStateAsDeadzone() {
        var axes = controller.state().axes();

        for (int i = 0; i < axes.size(); i++) {
            var axis = axes.get(i);
            var minDeadzone = axis + 0.08f;
            var deadzone = (float)Mth.clamp(0.05 * Math.ceil(minDeadzone / 0.05), 0, 0.95);

            if (Float.isNaN(deadzone)) {
                Controlify.LOGGER.warn("Deadzone for axis {} is NaN, using default deadzone.", i);
                deadzone = controller.defaultConfig().getDeadzone(i);
            }

            controller.config().setDeadzone(i, deadzone);
        }
    }

    private boolean stateChanged() {
        var amt = 0.0001f;
        
        return controller.state().axes().stream()
                .anyMatch(axis -> Math.abs(axis - controller.prevState().axes().get(controller.state().axes().indexOf(axis))) > amt);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
