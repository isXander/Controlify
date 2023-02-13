package dev.isxander.controlify.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.math.RoundingMode;

public class ControllerDeadzoneCalibrationScreen extends Screen {
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");

    protected final Controller controller;
    private final Screen parent;

    private MultiLineLabel waitLabel, infoLabel, completeLabel;

    protected Button readyButton;

    protected boolean calibrating = false, calibrated = false;
    protected int calibrationTicks = 0;

    public ControllerDeadzoneCalibrationScreen(Controller controller, Screen parent) {
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
        }
    }

    private void useCurrentStateAsDeadzone() {
        var rawAxes = controller.state().rawAxes();

        var minDeadzoneLS = Math.max(rawAxes.leftStickX(), rawAxes.leftStickY()) + 0.08f;
        var deadzoneLS = (float)Mth.clamp(0.05 * Math.ceil(minDeadzoneLS / 0.05), 0, 0.95);

        var minDeadzoneRS = Math.max(rawAxes.rightStickX(), rawAxes.rightStickY()) + 0.08f;
        var deadzoneRS = (float)Mth.clamp(0.05 * Math.ceil(minDeadzoneRS / 0.05), 0, 0.95);

        controller.config().leftStickDeadzone = deadzoneLS;
        controller.config().rightStickDeadzone = deadzoneRS;
    }

    private boolean stateChanged() {
        var amt = 0.0001f;

        var lsX = controller.state().rawAxes().leftStickX();
        var prevLsX = controller.prevState().rawAxes().leftStickX();
        if (Math.abs(lsX - prevLsX) > amt)
            return true;

        var lsY = controller.state().rawAxes().leftStickY();
        var prevLsY = controller.prevState().rawAxes().leftStickY();
        if (Math.abs(lsY - prevLsY) > amt)
            return true;

        var rsX = controller.state().rawAxes().rightStickX();
        var prevRsX = controller.prevState().rawAxes().rightStickX();
        if (Math.abs(rsX - prevRsX) > amt)
            return true;

        var rsY = controller.state().rawAxes().rightStickY();
        var prevRsY = controller.prevState().rawAxes().rightStickY();
        if (Math.abs(rsY - prevRsY) > amt)
            return true;

        return false;
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
