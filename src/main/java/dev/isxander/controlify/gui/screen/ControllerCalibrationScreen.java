package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.ControllerManager;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Controller calibration screen does a few things:
 * <ul>
 *     <li>Calculates deadzones</li>
 *     <li>Does gyroscope calibration</li>
 *     <li>Detects triggers on unmapped joysticks</li>
 * </ul>
 */
public class ControllerCalibrationScreen extends Screen implements DontInteruptScreen {
    private static final int CALIBRATION_TIME = 100;
    private static final ResourceLocation GREEN_BACK_BAR = new ResourceLocation("boss_bar/green_background");
    private static final ResourceLocation GREEN_FRONT_BAR = new ResourceLocation("boss_bar/green_progress");

    protected final Controller<?, ?> controller;
    private final Supplier<Screen> parent;

    private MultiLineLabel waitLabel, infoLabel, completeLabel;

    protected Button readyButton, laterButton;

    protected boolean calibrating = false, calibrated = false;
    protected int calibrationTicks = 0;

    private final double[] axisData;
    private GamepadState.GyroState accumulatedGyroVelocity = new GamepadState.GyroState();

    public ControllerCalibrationScreen(Controller<?, ?> controller, Screen parent) {
        this(controller, () -> parent);
    }

    public ControllerCalibrationScreen(Controller<?, ?> controller, Supplier<Screen> parent) {
        super(Component.translatable("controlify.calibration.title"));
        this.controller = controller;
        this.parent = parent;
        this.axisData = new double[controller.axisCount() * CALIBRATION_TIME];
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
        renderBackground(graphics, mouseX, mouseY, delta);

        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredString(font, Component.translatable("controlify.calibration.title", controller.name()).withStyle(ChatFormatting.BOLD), width / 2, 8, -1);

        graphics.pose().pushPose();
        graphics.pose().scale(2f, 2f, 1f);

        float progress = (calibrationTicks - 1 + delta) / 100f;
        drawBar(graphics, width / 2 / 2, 30 / 2, progress);

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

    private void drawBar(GuiGraphics graphics, int centerX, int y, float progress) {
        int width = Mth.lerpDiscrete(1 - (float)Math.pow(1 - progress, 3), 0, 182);

        int x = centerX - 182 / 2;
        graphics.blitSprite(GREEN_BACK_BAR, 182, 5, 0, 0, x, y, 182, 5);
        if (width > 0) {
            graphics.blitSprite(GREEN_FRONT_BAR, 182, 5, 0, 0, x, y, width, 5);
        }
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
            Arrays.fill(axisData, 0);
            accumulatedGyroVelocity = new GamepadState.GyroState();
        }

        if (calibrationTicks < CALIBRATION_TIME) {
            processAxisData(calibrationTicks);
            processGyroData();

            calibrationTicks++;
        } else {
            calibrateAxis();
            generateGyroCalibration();

            calibrating = false;
            calibrated = true;
            readyButton.active = true;
            readyButton.setMessage(Component.translatable("controlify.calibration.done"));

            controller.config().deadzonesCalibrated = true;
            controller.config().delayedCalibration = false;
            // no need to save because of setCurrentController

            Controlify.instance().setCurrentController(controller);
        }
    }

    private void processAxisData(int tick) {
        var axes = controller.state().rawAxes();

        System.arraycopy(axes.stream().mapToDouble(a -> a).toArray(), 0, axisData, tick * axes.size(), axes.size());
    }

    private void processGyroData() {
        if (controller instanceof GamepadController gamepad && gamepad.hasGyro()) {
            accumulatedGyroVelocity.add(gamepad.drivers.gyroDriver().getGyroState());
        }
    }

    private void calibrateAxis() {
        int axisCount = controller.axisCount();
        for (int axis = 0; axis < axisCount; axis++) {
            boolean triggerAxis = true;
            float maxAbs = 0;

            for (int tick = 0; tick < CALIBRATION_TIME; tick++) {
                float axisValue = (float) axisData[tick * axisCount + axis];

                if (axisValue != -1) {
                    triggerAxis = false;
                }

                maxAbs = Math.max(maxAbs, Math.abs(axisValue));
            }

            if (triggerAxis && controller instanceof JoystickController<?> joystick && joystick.mapping() instanceof UnmappedJoystickMapping mapping) {
                joystick.config().setDeadzone(axis, 0.0f);
                joystick.config().setTriggerAxis(axis, true);
                mapping.setTriggerAxes(axis, true);
            } else {
                controller.config().setDeadzone(axis, maxAbs + 0.08f);
            }
        }
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
