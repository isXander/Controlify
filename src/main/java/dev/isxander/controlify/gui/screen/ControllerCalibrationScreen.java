package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.ControllerManager;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ControllerCalibrationScreen extends Screen {
    private static final int CALIBRATION_TIME = 100;
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");

    protected final Controller<?, ?> controller;
    private final Supplier<Screen> parent;

    private MultiLineLabel waitLabel, infoLabel, completeLabel;

    protected Button readyButton, laterButton;

    protected boolean calibrating = false, calibrated = false;
    protected int calibrationTicks = 0;

    private final Map<Integer, double[]> deadzoneCalibration = new HashMap<>();
    private GamepadState.GyroState accumulatedGyroVelocity = new GamepadState.GyroState();

    public ControllerCalibrationScreen(Controller<?, ?> controller, Screen parent) {
        this(controller, () -> parent);
    }

    public ControllerCalibrationScreen(Controller<?, ?> controller, Supplier<Screen> parent) {
        super(Component.translatable("controlify.calibration.title"));
        this.controller = controller;
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(readyButton =
                Button.builder(Component.translatable("controlify.calibration.ready"), btn -> onButtonPress())
                        .width(150)
                        .pos(this.width / 2 - 150 - 5, this.height - 8 - 20)
                        .build()
        );
        addRenderableWidget(laterButton =
                Button.builder(Component.translatable("controlify.calibration.later"), btn -> onLaterButtonPress())
                        .width(150)
                        .pos(this.width / 2 + 5, this.height - 8 - 20)
                        .tooltip(Tooltip.create(Component.translatable("controlify.calibration.later.tooltip")))
                        .build()
        );


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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);

        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredString(font, Component.translatable("controlify.calibration.title", controller.name()).withStyle(ChatFormatting.BOLD), width / 2, 8, -1);

        graphics.pose().pushPose();
        graphics.pose().scale(2f, 2f, 1f);
        drawBar(graphics, width / 2 / 2, 30 / 2, 1f, 0);
        var progress = (calibrationTicks - 1 + delta) / 100f;
        if (progress > 0)
            drawBar(graphics, width / 2 / 2, 30 / 2, progress, 5);
        graphics.pose().popPose();

        MultiLineLabel label;
        if (calibrating) label = waitLabel;
        else if (calibrated) label = completeLabel;
        else label = infoLabel;

        label.renderCentered(graphics, width / 2, 55);

        graphics.pose().pushPose();
        float scale = Math.min(3f, (readyButton.getY() - (55 + font.lineHeight * label.getLineCount()) - 2) / 64f);
        graphics.pose().translate(width / 2f - 32 * scale, 55 + font.lineHeight * label.getLineCount(), 0f);
        graphics.pose().scale(scale, scale, 1f);
        graphics.blit(controller.icon(), 0, 0, 0f, 0f, 64, 64, 64, 64);
        graphics.pose().popPose();
    }

    private void drawBar(GuiGraphics graphics, int centerX, int y, float progress, int vOffset) {
        progress = 1 - (float)Math.pow(1 - progress, 3);

        int x = centerX - 182 / 2;
        graphics.blit(GUI_BARS_LOCATION, x, y, 0, 30 + vOffset, (int)(progress * 182), 5);
    }

    @Override
    public void tick() {
        if (!ControllerManager.isControllerConnected(controller.uid())) {
            onClose();
            return;
        }

        if (!calibrating)
            return;

        if (stateChanged()) {
            calibrationTicks = 0;
            deadzoneCalibration.clear();
            accumulatedGyroVelocity = new GamepadState.GyroState();
        }

        if (calibrationTicks < CALIBRATION_TIME) {
            processDeadzoneData(calibrationTicks);
            processGyroData();

            calibrationTicks++;
        } else {
            applyDeadzones();
            generateGyroCalibration();

            calibrating = false;
            calibrated = true;
            readyButton.active = true;
            readyButton.setMessage(Component.translatable("controlify.calibration.done"));

            controller.config().deadzonesCalibrated = true;
            controller.config().delayedCalibration = false;
            Controlify.instance().config().save();
        }
    }

    private void processDeadzoneData(int tick) {
        var axes = controller.state().rawAxes();

        for (int i = 0; i < axes.size(); i++) {
            var axis = Math.abs(axes.get(i));
            deadzoneCalibration.computeIfAbsent(i, k -> new double[CALIBRATION_TIME])[tick] = axis;
        }
    }

    private void processGyroData() {
        if (controller instanceof GamepadController gamepad && gamepad.hasGyro()) {
            accumulatedGyroVelocity.add(gamepad.drivers.gyroDriver().getGyroState());
        }
    }

    private void applyDeadzones() {
        deadzoneCalibration.forEach((i, data) -> {
            var max = Arrays.stream(data).max().orElseThrow();
            controller.config().setDeadzone(i, (float) max + 0.05f);
        });
    }

    private void generateGyroCalibration() {
        if (controller instanceof GamepadController gamepad && gamepad.hasGyro()) {
            gamepad.config().gyroCalibration = accumulatedGyroVelocity.div(CALIBRATION_TIME);
        }
    }

    private boolean stateChanged() {
        var amt = 0.4f;
        
        return controller.state().axes().stream()
                .anyMatch(axis -> Math.abs(axis - controller.prevState().axes().get(controller.state().axes().indexOf(axis))) > amt);
    }

    private void onButtonPress() {
        if (!calibrated) {
            startCalibration();

            removeWidget(laterButton);
            readyButton.setX(this.width / 2 - 75);
        } else
            onClose();
    }

    private void onLaterButtonPress() {
        if (!calibrated) {
            if (!controller.config().deadzonesCalibrated) {
                controller.config().delayedCalibration = true;
                Controlify.instance().config().setDirty();
                Controlify.instance().setCurrentController(null);
            }

            onClose();
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent.get());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
