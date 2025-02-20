package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.input.ControllerState;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.DeadzoneGroup;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.utils.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    protected final Controlify controlify;
    protected final ControllerManager controllerManager;
    protected final ControllerEntity controller;
    private final Supplier<Screen> parent;

    private MultiLineLabel waitLabel, infoLabel, completeLabel;

    protected Button readyButton, laterButton;

    protected boolean calibrating = false, calibrated = false;
    protected int calibrationTicks = 0;

    @Nullable
    private final Map<ResourceLocation, float[]> axisData;
    private GyroState accumulatedGyroVelocity = new GyroState();

    public ControllerCalibrationScreen(ControllerEntity controller, Screen parent) {
        this(controller, () -> parent);
    }

    public ControllerCalibrationScreen(ControllerEntity controller, Supplier<Screen> parent) {
        super(Component.translatable("controlify.calibration.title"));
        this.controlify = Controlify.instance();
        this.controllerManager = controlify.getControllerManager().orElseThrow();
        this.controller = controller;
        this.parent = parent;

        Optional<InputComponent> inputOpt = controller.input();
        this.axisData = inputOpt.map(inputComponent -> new HashMap<ResourceLocation, float[]>(inputComponent.axisCount())).orElse(null);
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
        /*? if >=1.20.4 {*/
        renderBackground(graphics, mouseX, mouseY, delta);
        /*?} else {*/
        /*renderBackground(graphics);
        *//*?}*/

        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredString(font, Component.translatable("controlify.calibration.title", controller.name()).withStyle(ChatFormatting.BOLD), width / 2, 8, -1);

        graphics.pose().pushPose();
        graphics.pose().scale(2f, 2f, 1f);

        float progress = (calibrationTicks - 1 + delta) / 100f;
        progress = 1 - (float)Math.pow(1 - progress, 3);
        ClientUtils.drawBar(graphics, width / 2 / 2, 30 / 2, progress);

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
        ClientUtils.drawSprite(graphics, controller.info().type().getIconSprite(), 0, 0, 64, 64);

        graphics.pose().popPose();
    }

    @Override
    public void tick() {
        if (!controllerManager.isControllerConnected(controller.uid())) {
            onClose();
            return;
        }

        if (!calibrating)
            return;

        if (stateChanged()) {
            calibrationTicks = 0;
            if (axisData != null)
                axisData.clear();
            accumulatedGyroVelocity = new GyroState();
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

            controller.input().map(input -> input.config().config()).ifPresent(config -> {
                config.deadzonesCalibrated = true;
                config.delayedCalibration = false;
            });
            controller.gyro().map(gyro -> gyro.config().config()).ifPresent(config -> {
                config.calibrated = true;
            });
            Controlify.instance().config().setDirty();
            Controlify.instance().config().saveIfDirty();
        }
    }

    private void processAxisData(int tick) {
        if (axisData == null)
            return;

        InputComponent input = controller.input().orElseThrow();
        ControllerState state = input.rawStateNow();
        for (DeadzoneGroup group : input.getDeadzoneGroups().values()) {
            float[] axisData = this.axisData.computeIfAbsent(group.name(), k -> new float[CALIBRATION_TIME]);

            float max = 0;
            for (ResourceLocation axis : group.axes()) {
                max = Math.max(max, Math.abs(state.getAxisState(axis)));
            }

            axisData[tick] = max;
        }
    }

    private void processGyroData() {
        controller.gyro().ifPresent(gyro -> {
            accumulatedGyroVelocity.add(gyro.getState());
        });
    }

    private void calibrateAxis() {
        if (axisData == null)
            return;
        InputComponent input = controller.input().orElseThrow();

        input.config().config().deadzones.clear();

        for (DeadzoneGroup group : input.getDeadzoneGroups().values()) {
            float[] axisData = this.axisData.get(group.name());
            if (axisData == null)
                continue;

            float maxAbs = 0;
            for (int tick = 0; tick < CALIBRATION_TIME; tick++) {
                float axisValue = axisData[tick];
                maxAbs = Math.max(maxAbs, Math.abs(axisValue));
            }

            input.config().config().deadzones.put(group.name(), maxAbs + 0.08f);
        }
    }

    private void generateGyroCalibration() {
        controller.gyro().ifPresent(gyro -> {
            gyro.config().config().calibration = accumulatedGyroVelocity.div(CALIBRATION_TIME);
        });

    }

    private boolean stateChanged() {
        InputComponent input = controller.input().orElseThrow();

        var amt = 0.4f;

        for (ResourceLocation axis : input.rawStateNow().getAxes()) {
            float[] axisData = this.axisData.get(axis);
            if (axisData == null)
                continue;

            float axisValue = input.rawStateNow().getAxisState(axis);
            float prevAxisValue = input.rawStateThen().getAxisState(axis);

            if (Math.abs(axisValue - prevAxisValue) > amt)
                return true;
        }

        return false;
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
            boolean dirty = false;
            dirty |= controller.input()
                    .map(input -> input.config().config().delayedCalibration = true)
                    .orElse(false);
            dirty |= controller.gyro()
                    .map(gyro -> gyro.config().config().delayedCalibration = true)
                    .orElse(false);

            if (dirty) {
                Controlify.instance().config().setDirty();
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
