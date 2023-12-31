package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepademulated.EmulatedGamepadController;
import dev.isxander.controlify.controller.gamepademulated.mapping.AxisMapping;
import dev.isxander.controlify.controller.gamepademulated.mapping.ButtonMapping;
import dev.isxander.controlify.controller.gamepademulated.mapping.UserGamepadMapping;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.driver.joystick.BasicJoystickState;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.utils.Log;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public class GamepadEmulationMappingCreatorScreen extends Screen implements ScreenControllerEventListener, DontInteruptScreen {
    private final EmulatedGamepadController controller;
    private final UserGamepadMapping.Builder mappingBuilder = new UserGamepadMapping.Builder();

    private int delayTillNextStage = -1;

    private final List<Stage> STAGES = List.of(
            new ButtonStage(mappingBuilder::faceDownButton, button("face_down")),
            new ButtonStage(mappingBuilder::faceLeftButton, button("face_left")),
            new ButtonStage(mappingBuilder::faceRightButton, button("face_right")),
            new ButtonStage(mappingBuilder::faceUpButton, button("face_up")),
            new ButtonStage(mappingBuilder::leftBumper, button("left_bumper")),
            new ButtonStage(mappingBuilder::rightBumper, button("right_bumper")),
            new ButtonStage(mappingBuilder::leftSpecial, button("left_special")),
            new ButtonStage(mappingBuilder::rightSpecial, button("right_special")),
            new ButtonStage(mappingBuilder::leftStickDown, button("left_stick_down")),
            new ButtonStage(mappingBuilder::rightStickDown, button("right_stick_down")),
            new ButtonStage(mappingBuilder::dpadUp, button("dpad_up")),
            new ButtonStage(mappingBuilder::dpadLeft, button("dpad_left")),
            new ButtonStage(mappingBuilder::dpadDown, button("dpad_down")),
            new ButtonStage(mappingBuilder::dpadRight, button("dpad_right")),
            new AxisStage(mappingBuilder::leftStickX, axis("left_stick", true)),
            new AxisStage(mappingBuilder::leftStickY, axis("left_stick", false)),
            new AxisStage(mappingBuilder::rightStickX, axis("right_stick", true)),
            new AxisStage(mappingBuilder::rightStickY, axis("right_stick", false)),
            new AxisStage(mappingBuilder::triggerLeft, Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.left_trigger")),
            new AxisStage(mappingBuilder::triggerRight, Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.right_trigger"))
    );
    private Stage currentStage = null;

    private final Screen lastScreen;

    public GamepadEmulationMappingCreatorScreen(EmulatedGamepadController controller, Screen lastScreen) {
        super(Component.literal("Gamepad Emulation Mapping Creator"));
        this.controller = controller;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {

    }

    @Override
    public void onControllerInput(Controller<?, ?> controller) {
        if (controller != this.controller) return;

        if (currentStage == null) {
            this.setStage(STAGES.get(0));
        } else if (currentStage.isSatisfied() && delayTillNextStage == -1) {
            int index = STAGES.indexOf(currentStage);
            if (index == STAGES.size() - 1) {
                onClose();
            } else {
                delayTillNextStage = 20;
            }
        }

        if (!currentStage.isSatisfied()) {
            BasicJoystickState stateNow = this.controller.joyState();
            BasicJoystickState statePrev = this.controller.prevJoyState();

            if (currentStage instanceof ButtonStage buttonStage) {
                processButtonStage(buttonStage, stateNow, statePrev);
            } else if (currentStage instanceof AxisStage axisStage) {
                processAxisStage(axisStage, stateNow, statePrev);
            }
        }
    }

    @Override
    public void tick() {
        if (delayTillNextStage >= 0) {
            delayTillNextStage--;
            if (delayTillNextStage == -1) {
                int index = STAGES.indexOf(currentStage);
                this.setStage(STAGES.get(index + 1));
            }
        }
    }

    private void processButtonStage(ButtonStage buttonStage, BasicJoystickState stateNow, BasicJoystickState statePrev) {
        for (int i = 0; i < statePrev.buttons().length; i++) {
            boolean now = stateNow.buttons()[i];
            boolean prev = statePrev.buttons()[i];
            if (now != prev) {
                ButtonMapping mapping = new ButtonMapping.FromButton(i, !now);
                buttonStage.setMapping(mapping);
                return;
            }
        }

        for (int i = 0; i < statePrev.axes().length; i++) {
            float now = stateNow.axes()[i];
            float prev = statePrev.axes()[i];

            float diff = prev - now;
            if (Math.abs(diff) > 0.3f) {
                ButtonMapping mapping = new ButtonMapping.FromAxis(i, diff > 0 ? ButtonMapping.FromAxis.NORMAL : ButtonMapping.FromAxis.INVERTED);
                buttonStage.setMapping(mapping);
                return;
            }
        }

        for (int i = 0; i < statePrev.hats().length; i++) {
            JoystickState.HatState now = stateNow.hats()[i];
            JoystickState.HatState prev = statePrev.hats()[i];
            if (now != prev) {
                ButtonMapping mapping = new ButtonMapping.FromHat(i, prev, false);
                buttonStage.setMapping(mapping);
                return;
            }
        }
    }

    private void processAxisStage(AxisStage axisStage, BasicJoystickState stateNow, BasicJoystickState statePrev) {
        for (int i = 0; i < statePrev.buttons().length; i++) {
            boolean now = stateNow.buttons()[i];
            boolean prev = statePrev.buttons()[i];
            if (now != prev) {
                AxisMapping mapping = new AxisMapping.FromButton(i, prev ? 1 : 0, now ? 1 : 0);
                axisStage.setMapping(mapping);
                return;
            }
        }

        for (int i = 0; i < statePrev.axes().length; i++) {
            float now = stateNow.axes()[i];
            float prev = statePrev.axes()[i];

            float diff = prev - now;
            if (Math.abs(diff) > 0.3f) {
                // automatically determine if this axis is a trigger or not
                boolean isTrigger = prev == -1;

                AxisMapping mapping = new AxisMapping.FromAxis(i, -1, 1, isTrigger ? 0 : -1, 1);
                axisStage.setMapping(mapping);
                return;
            }
        }

        for (int i = 0; i < statePrev.hats().length; i++) {
            JoystickState.HatState now = stateNow.hats()[i];
            JoystickState.HatState prev = statePrev.hats()[i];
            if (now != prev) {
                AxisMapping mapping = new AxisMapping.FromHat(i, prev, 0, 1);
                axisStage.setMapping(mapping);
                return;
            }
        }
    }

    private void setStage(Stage stage) {
        currentStage = stage;
        Log.LOGGER.info("Now mapping " + stage.name().getString());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
        controller.config().mapping = mappingBuilder.build();
        Controlify.instance().config().save();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);

        if (currentStage != null)
            guiGraphics.drawCenteredString(font, currentStage.name().getString(), width / 2, height / 2, 0xFFFFFF);
    }

    private static Component button(String buttonName) {
        return Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.button", Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction." + buttonName));
    }

    private static Component axis(String axisName, boolean horizontal) {
        Component axis = Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction." + axisName);
        return horizontal
                ? Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.axis_x", axis)
                : Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.axis_y", axis);
    }

    private sealed abstract static class Stage permits ButtonStage, AxisStage {
        private final Component name;
        protected boolean satisfied;

        protected Stage(Component name) {
            this.name = name;
        }

        public Component name() {
            return name;
        }

        public boolean isSatisfied() {
            return satisfied;
        }
    }

    private static final class ButtonStage extends Stage {
        private final Function<ButtonMapping, UserGamepadMapping.Builder> builder;

        private ButtonStage(Function<ButtonMapping, UserGamepadMapping.Builder> builder, Component name) {
            super(name);
            this.builder = builder;
        }

        public void setMapping(ButtonMapping mapping) {
            builder.apply(mapping);
            satisfied = true;
        }
    }

    private static final class AxisStage extends Stage {
        private final Function<AxisMapping, UserGamepadMapping.Builder> builder;

        private AxisStage(Function<AxisMapping, UserGamepadMapping.Builder> builder, Component name) {
            super(name);
            this.builder = builder;
        }

        public void setMapping(AxisMapping mapping) {
            builder.apply(mapping);
            satisfied = true;
        }
    }
}
