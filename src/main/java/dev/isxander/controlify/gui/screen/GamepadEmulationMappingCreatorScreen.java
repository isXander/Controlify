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
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.utils.ClientUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public class GamepadEmulationMappingCreatorScreen extends Screen implements ScreenControllerEventListener, ScreenProcessorProvider, DontInteruptScreen {
    private final EmulatedGamepadController controller;
    private final UserGamepadMapping.Builder mappingBuilder = new UserGamepadMapping.Builder();
    private final ScreenProcessor<GamepadEmulationMappingCreatorScreen> screenProcessor = new ScreenProcessorImpl(this);

    private int delayTillNextStage = 20;

    private final List<Stage> STAGES = List.of(
            new ButtonStage(mappingBuilder::faceDownButton, button("face_down"), "face_down", "faceview"),
            new ButtonStage(mappingBuilder::faceLeftButton, button("face_left"), "face_left", "faceview"),
            new ButtonStage(mappingBuilder::faceRightButton, button("face_right"), "face_right", "faceview"),
            new ButtonStage(mappingBuilder::faceUpButton, button("face_up"), "face_up", "faceview"),
            new ButtonStage(mappingBuilder::leftBumper, button("left_bumper"), "left_bumper", "triggerview"),
            new ButtonStage(mappingBuilder::rightBumper, button("right_bumper"), "right_bumper", "triggerview"),
            new ButtonStage(mappingBuilder::leftSpecial, button("left_special"), "left_special", "faceview"),
            new ButtonStage(mappingBuilder::rightSpecial, button("right_special"), "right_special", "faceview"),
            new ButtonStage(mappingBuilder::leftStickDown, button("left_stick_down"), "left_stick_press", "faceview"),
            new ButtonStage(mappingBuilder::rightStickDown, button("right_stick_down"), "right_stick_press", "faceview"),
            new ButtonStage(mappingBuilder::dpadUp, button("dpad_up"), "dpad_up", "faceview"),
            new ButtonStage(mappingBuilder::dpadLeft, button("dpad_left"), "dpad_left", "faceview"),
            new ButtonStage(mappingBuilder::dpadDown, button("dpad_down"), "dpad_down", "faceview"),
            new ButtonStage(mappingBuilder::dpadRight, button("dpad_right"), "dpad_right", "faceview"),
            new AxisStage(mappingBuilder::leftStickX, axis("left_stick", true), "left_stick_x", "faceview"),
            new AxisStage(mappingBuilder::leftStickY, axis("left_stick", false), "left_stick_y", "faceview"),
            new AxisStage(mappingBuilder::rightStickX, axis("right_stick", true), "right_stick_x", "faceview"),
            new AxisStage(mappingBuilder::rightStickY, axis("right_stick", false), "right_stick_y", "faceview"),
            new TriggerAxisStage(mappingBuilder::triggerLeft, Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.left_trigger"), "left_trigger", "triggerview"),
            new TriggerAxisStage(mappingBuilder::triggerRight, Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.right_trigger"), "right_trigger", "triggerview")
    );
    private Stage currentStage = null;

    private Button goBackButton;
    private final Screen lastScreen;

    public GamepadEmulationMappingCreatorScreen(EmulatedGamepadController controller, Screen lastScreen) {
        super(Component.literal("Gamepad Emulation Mapping Creator"));
        this.controller = controller;
        this.lastScreen = lastScreen;

        this.mappingBuilder.inputDriverName(controller.drivers.basicJoystickInputDriver().getBasicJoystickDetails());
        controller.config().mapping = UserGamepadMapping.NO_MAPPING;
    }

    @Override
    protected void init() {
        addRenderableWidget(
                goBackButton = Button.builder(
                        Component.translatable("controlify.gui.gamepademulationmappingcreator.go_back"),
                        button -> goBackStage()
                )
                        .bounds(width / 2 - 152, height - 60, 150, 20)
                        .tooltip(Tooltip.create(Component.translatable("controlify.gui.gamepademulationmappingcreator.go_back.tooltip")))
                        .build()
        );
        addRenderableWidget(
                Button.builder(
                        Component.translatable("controlify.gui.gamepademulationmappingcreator.no_map"),
                        button -> mapAsNone()
                )
                        .bounds(width / 2 + 2, height - 60, 150, 20)
                        .tooltip(Tooltip.create(Component.translatable("controlify.gui.gamepademulationmappingcreator.no_map.tooltip")))
                        .build()
        );
        goBackButton.active = false;
    }

    @Override
    public void tick() {
        if (delayTillNextStage >= 0) {
            delayTillNextStage--;
            if (delayTillNextStage == -1) {
                if (currentStage == null) {
                    this.setStage(STAGES.get(0));
                } else {
                    int index = STAGES.indexOf(currentStage);
                    int nextIndex = index + 1;
                    if (nextIndex >= STAGES.size()) {
                        onClose();
                    } else {
                        this.setStage(STAGES.get(index + 1));
                        goBackButton.active = true;
                    }
                }
            }
        } else {
            if (currentStage == null) {
                this.setStage(STAGES.get(0));
            } else if (currentStage.isSatisfied() && delayTillNextStage == -1) {
                delayTillNextStage = 20;
            }

            if (currentStage != null && !currentStage.isSatisfied()) {
                BasicJoystickState stateNow = this.controller.joyState();
                BasicJoystickState statePrev = this.controller.prevJoyState();

                if (currentStage instanceof ButtonStage buttonStage) {
                    processButtonStage(buttonStage, stateNow, statePrev);
                } else if (currentStage instanceof AxisStage axisStage) {
                    processAxisStage(axisStage, stateNow, statePrev);
                }
            }
        }
    }

    private void processButtonStage(ButtonStage buttonStage, BasicJoystickState stateNow, BasicJoystickState statePrev) {
        for (int i = 0; i < Math.min(stateNow.buttons().length, statePrev.buttons().length); i++) {
            boolean now = stateNow.buttons()[i];
            boolean prev = statePrev.buttons()[i];
            if (now != prev) {
                ButtonMapping mapping = new ButtonMapping.FromButton(i, !now);
                buttonStage.setMapping(mapping);
                return;
            }
        }

        for (int i = 0; i < Math.min(stateNow.axes().length, statePrev.axes().length); i++) {
            float now = stateNow.axes()[i];
            float prev = statePrev.axes()[i];

            float diff = prev - now;
            if (Math.abs(diff) > 0.3f) {
                ButtonMapping mapping = new ButtonMapping.FromAxis(i, diff > 0 ? ButtonMapping.FromAxis.NORMAL : ButtonMapping.FromAxis.INVERTED);
                buttonStage.setMapping(mapping);
                return;
            }
        }

        for (int i = 0; i < Math.min(stateNow.hats().length, statePrev.hats().length); i++) {
            JoystickState.HatState now = stateNow.hats()[i];
            JoystickState.HatState prev = statePrev.hats()[i];
            if (now != prev) {
                ButtonMapping mapping = new ButtonMapping.FromHat(i, now, false);
                buttonStage.setMapping(mapping);
                return;
            }
        }
    }

    private void processAxisStage(AxisStage axisStage, BasicJoystickState stateNow, BasicJoystickState statePrev) {
        for (int i = 0; i < Math.min(stateNow.buttons().length, statePrev.buttons().length); i++) {
            boolean now = stateNow.buttons()[i];
            boolean prev = statePrev.buttons()[i];
            if (now != prev) {
                AxisMapping mapping = new AxisMapping.FromButton(i, prev ? 1 : 0, now ? 1 : 0);
                axisStage.setMapping(mapping);
                return;
            }
        }

        for (int i = 0; i < Math.min(stateNow.axes().length, statePrev.axes().length); i++) {
            float now = stateNow.axes()[i];
            float prev = statePrev.axes()[i];

            float diff = prev - now;
            if (Math.abs(diff) > 0.3f) {
                // automatically determine if this axis is a trigger or not
                boolean isTrigger = axisStage instanceof TriggerAxisStage && prev < -0.5f;

                AxisMapping mapping = new AxisMapping.FromAxis(i, -1, 1, isTrigger ? 0 : -1, 1);
                axisStage.setMapping(mapping);
                return;
            }
        }

        for (int i = 0; i < Math.min(stateNow.hats().length, statePrev.hats().length); i++) {
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
    }

    private void mapAsNone() {
        if (currentStage instanceof ButtonStage stage) {
            stage.setMapping(new ButtonMapping.FromNothing(false));
        } else if (currentStage instanceof AxisStage stage) {
            stage.setMapping(new AxisMapping.FromNothing(0f));
        }
        delayTillNextStage = 0;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
        controller.config().mapping = mappingBuilder.build();
        Controlify.instance().config().save();
    }

    private void goBackStage() {
        int index = STAGES.indexOf(currentStage);
        int nextIndex = index - 1;
        if (nextIndex >= 0) {
            this.setStage(STAGES.get(index - 1));
            currentStage.unsatisfy();
        }
        goBackButton.active = nextIndex > 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);

        guiGraphics.drawCenteredString(font, Component.translatable("controlify.gui.gamepademulationmappingcreator.title"), width / 2, 15, 0xFFFFFF);

        guiGraphics.drawCenteredString(
                font,
                currentStage == null ? Component.translatable("controlify.gui.gamepademulationmappingcreator.please_wait") : currentStage.name(),
                width / 2, height - 20,
                0xFFFFFF
        );

        int safeZone = Math.min(width, height) - 30;
        float scale = safeZone / 32f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(width / 2f, -5, 0);
        guiGraphics.pose().translate(-32 * scale / 2f, 0, 0);
        guiGraphics.pose().scale(scale, scale, 1f);

        float colour = currentStage != null && currentStage.isSatisfied() ? 0.46f : 1f;
        guiGraphics.setColor(colour, colour, colour, 1f);

        if (currentStage != null && currentStage.backgroundTexture() != null) {
            guiGraphics.blit(currentStage.backgroundTexture(), 0, 0, 0, 0, 32, 32, 32, 32);
        }

        if (currentStage == null || !currentStage.isSatisfied()) {
            ResourceLocation texture = currentStage != null ? currentStage.foregroundTexture() : new ResourceLocation("controlify", "textures/gui/controllerdiagram/faceview.png");
            guiGraphics.blit(texture, 0, 0, 0, 0, 32, 32, 32, 32);
        }

        guiGraphics.setColor(1f, 1f, 1f, 1f);

        guiGraphics.pose().popPose();

        float progress = currentStage != null ? (float) (STAGES.indexOf(currentStage) + 1) / STAGES.size() : 0;
        ClientUtils.drawBar(guiGraphics, width / 2, height - 30, progress);
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

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return screenProcessor;
    }

    private sealed abstract static class Stage permits ButtonStage, AxisStage {
        private final Component name;
        private final ResourceLocation foregroundTexture, backgroundTexture;

        protected boolean satisfied;

        protected Stage(Component name, String foreground, String background) {
            this.name = name;
            this.foregroundTexture = new ResourceLocation("controlify", "textures/gui/controllerdiagram/" + foreground + ".png");
            this.backgroundTexture = new ResourceLocation("controlify", "textures/gui/controllerdiagram/" + background + ".png");
        }

        public Component name() {
            return name;
        }

        public ResourceLocation foregroundTexture() {
            return foregroundTexture;
        }

        public ResourceLocation backgroundTexture() {
            return backgroundTexture;
        }

        public boolean isSatisfied() {
            return satisfied;
        }

        public void unsatisfy() {
            satisfied = false;
        }
    }

    private static final class ButtonStage extends Stage {
        private final Function<ButtonMapping, UserGamepadMapping.Builder> builder;

        private ButtonStage(Function<ButtonMapping, UserGamepadMapping.Builder> builder, Component name, String foreground, String background) {
            super(name, foreground, background);
            this.builder = builder;
        }

        public void setMapping(ButtonMapping mapping) {
            builder.apply(mapping);
            satisfied = true;
        }
    }

    private static sealed class AxisStage extends Stage permits TriggerAxisStage {
        private final Function<AxisMapping, UserGamepadMapping.Builder> builder;

        private AxisStage(Function<AxisMapping, UserGamepadMapping.Builder> builder, Component name, String foreground, String background) {
            super(name, foreground, background);
            this.builder = builder;
        }

        public void setMapping(AxisMapping mapping) {
            builder.apply(mapping);
            satisfied = true;
        }
    }

    private static final class TriggerAxisStage extends AxisStage {
        private TriggerAxisStage(Function<AxisMapping, UserGamepadMapping.Builder> builder, Component name, String foreground, String background) {
            super(builder, name, foreground, background);
        }
    }

    private static class ScreenProcessorImpl extends ScreenProcessor<GamepadEmulationMappingCreatorScreen> {
        public ScreenProcessorImpl(GamepadEmulationMappingCreatorScreen screen) {
            super(screen);
        }

        @Override
        public void onControllerUpdate(Controller<?, ?> controller) {

        }
    }
}
