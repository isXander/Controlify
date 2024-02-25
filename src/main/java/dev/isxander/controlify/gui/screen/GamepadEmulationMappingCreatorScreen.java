package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.*;
import dev.isxander.controlify.controller.input.mapping.MapType;
import dev.isxander.controlify.controller.input.mapping.MappingEntry;
import dev.isxander.controlify.controller.input.mapping.UserGamepadMapping;
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

public class GamepadEmulationMappingCreatorScreen extends Screen implements ScreenControllerEventListener, ScreenProcessorProvider, DontInteruptScreen {
    private final InputComponent inputComponent;
    private final UserGamepadMapping.Builder mappingBuilder = new UserGamepadMapping.Builder();
    private final ScreenProcessor<GamepadEmulationMappingCreatorScreen> screenProcessor = new ScreenProcessorImpl(this);

    private int delayTillNextStage = 20;

    public static final List<MappingStage> GAMEPAD_STAGES = List.of(
            new MappingStage(GamepadInputs.SOUTH_BUTTON, MapType.BUTTON, button("face_down"), "face_down", "faceview"),
            new MappingStage(GamepadInputs.WEST_BUTTON, MapType.BUTTON, button("face_left"), "face_left", "faceview"),
            new MappingStage(GamepadInputs.EAST_BUTTON, MapType.BUTTON, button("face_right"), "face_right", "faceview"),
            new MappingStage(GamepadInputs.NORTH_BUTTON, MapType.BUTTON, button("face_up"), "face_up", "faceview"),
            new MappingStage(GamepadInputs.LEFT_SHOULDER_BUTTON, MapType.BUTTON, button("left_bumper"), "left_bumper", "triggerview"),
            new MappingStage(GamepadInputs.RIGHT_SHOULDER_BUTTON, MapType.BUTTON, button("right_bumper"), "right_bumper", "triggerview"),
            new MappingStage(GamepadInputs.LEFT_STICK_BUTTON, MapType.BUTTON, button("left_special"), "left_special", "faceview"),
            new MappingStage(GamepadInputs.RIGHT_STICK_BUTTON, MapType.BUTTON, button("right_special"), "right_special", "faceview"),
            new MappingStage(GamepadInputs.LEFT_STICK_BUTTON, MapType.BUTTON, button("left_stick_down"), "left_stick_press", "faceview"),
            new MappingStage(GamepadInputs.RIGHT_STICK_BUTTON, MapType.BUTTON, button("right_stick_down"), "right_stick_press", "faceview"),
            new MappingStage(GamepadInputs.DPAD_UP_BUTTON, MapType.BUTTON, button("dpad_up"), "dpad_up", "faceview"),
            new MappingStage(GamepadInputs.DPAD_LEFT_BUTTON, MapType.BUTTON, button("dpad_left"), "dpad_left", "faceview"),
            new MappingStage(GamepadInputs.DPAD_DOWN_BUTTON, MapType.BUTTON, button("dpad_down"), "dpad_down", "faceview"),
            new MappingStage(GamepadInputs.DPAD_RIGHT_BUTTON, MapType.BUTTON, button("dpad_right"), "dpad_right", "faceview"),
            new MappingStage(GamepadInputs.LEFT_STICK_AXIS_LEFT, MapType.AXIS, axis("left_stick", true), "left_stick_x", "faceview"),
            new MappingStage(GamepadInputs.LEFT_STICK_AXIS_UP, MapType.AXIS, axis("left_stick", false), "left_stick_y", "faceview"),
            new MappingStage(GamepadInputs.RIGHT_STICK_AXIS_LEFT, MapType.AXIS, axis("right_stick", true), "right_stick_x", "faceview"),
            new MappingStage(GamepadInputs.RIGHT_STICK_AXIS_UP, MapType.AXIS, axis("right_stick", false), "right_stick_y", "faceview"),
            new MappingStage(GamepadInputs.LEFT_TRIGGER_AXIS, MapType.AXIS, Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.left_trigger"), "left_trigger", "triggerview"),
            new MappingStage(GamepadInputs.RIGHT_TRIGGER_AXIS, MapType.AXIS, Component.translatable("controlify.gui.gamepademulationmappingcreator.instruction.right_trigger"), "right_trigger", "triggerview")
    );

    private MappingStage currentStage = null;
    private final List<MappingStage> stages;

    private Button goBackButton;
    private final Screen lastScreen;

    public GamepadEmulationMappingCreatorScreen(InputComponent inputComponent, Screen lastScreen, List<MappingStage> stages) {
        super(Component.literal("Gamepad Emulation Mapping Creator"));
        this.inputComponent = inputComponent;
        this.lastScreen = lastScreen;
        this.stages = stages;

        inputComponent.confObj().mapping = UserGamepadMapping.NO_MAPPING;
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
                    this.setStage(stages.get(0));
                } else {
                    int index = stages.indexOf(currentStage);
                    int nextIndex = index + 1;
                    if (nextIndex >= stages.size()) {
                        onClose();
                    } else {
                        this.setStage(stages.get(index + 1));
                        goBackButton.active = true;
                    }
                }
            }
        } else {
            if (currentStage == null) {
                this.setStage(stages.get(0));
            } else if (currentStage.isSatisfied() && delayTillNextStage == -1) {
                delayTillNextStage = 20;
            }

            if (currentStage != null && !currentStage.isSatisfied()) {
                ControllerStateView stateNow = this.inputComponent.stateNow();
                ControllerStateView stateThen = this.inputComponent.stateThen();

                processStage(currentStage, stateNow, stateThen);
            }
        }
    }

    private void processStage(MappingStage buttonStage, ControllerStateView stateNow, ControllerStateView stateThen) {
        for (ResourceLocation button : stateNow.getButtons()) {
            boolean now = stateNow.isButtonDown(button);
            boolean prev = stateThen.isButtonDown(button);
            if (now != prev) {
                MappingEntry mapping = switch (buttonStage.outputType()) {
                    case BUTTON -> new MappingEntry.FromButton.ToButton(buttonStage.originInput(), button, !now);
                    case AXIS -> new MappingEntry.FromButton.ToAxis(buttonStage.originInput(), button, prev ? 1 : 0, now ? 1 : 0);
                    case HAT -> {
                        HatState state = now ? HatState.DOWN : HatState.UP;
                        yield new MappingEntry.FromButton.ToHat(buttonStage.originInput(), button, HatState.CENTERED, state);
                    }
                    case NOTHING -> null;
                };
                mappingBuilder.putMapping(mapping);
                return;
            }
        }

        for (ResourceLocation axis : stateNow.getAxes()) {
            float now = stateNow.getAxisState(axis);
            float prev = stateThen.getAxisState(axis);
            float diff = prev - now;
            if (Math.abs(diff) > 0.3f) {
                MappingEntry mapping = switch (buttonStage.outputType()) {
                    case BUTTON -> new MappingEntry.FromAxis.ToButton(buttonStage.originInput(), axis, 0.5f);
                    case AXIS -> new MappingEntry.FromAxis.ToAxis(buttonStage.originInput(), axis, -1, 1, -1, 1);
                    case HAT -> new MappingEntry.FromAxis.ToHat(buttonStage.originInput(), axis, 0.5f, diff > 0 ? HatState.UP : HatState.DOWN);
                    case NOTHING -> null;
                };
                mappingBuilder.putMapping(mapping);
                return;
            }
        }

        for (ResourceLocation hat : stateNow.getHats()) {
            HatState now = stateNow.getHatState(hat);
            HatState prev = stateThen.getHatState(hat);
            if (now != prev) {
                MappingEntry mapping = switch (buttonStage.outputType()) {
                    case BUTTON -> new MappingEntry.FromHat.ToButton(buttonStage.originInput(), hat, prev);
                    case AXIS -> new MappingEntry.FromHat.ToAxis(buttonStage.originInput(), hat, prev, 0, 1);
                    case HAT -> new MappingEntry.FromHat.ToHat(buttonStage.originInput(), hat);
                    case NOTHING -> null;
                };
                mappingBuilder.putMapping(mapping);
                return;
            }
        }
    }

    private void setStage(MappingStage stage) {
        currentStage = stage;
    }

    private void mapAsNone() {
        MappingEntry mapping = switch (currentStage.outputType()) {
            case BUTTON -> new MappingEntry.FromNothing.ToButton(currentStage.originInput(), false);
            case AXIS -> new MappingEntry.FromNothing.ToAxis(currentStage.originInput(), 0f);
            case HAT -> new MappingEntry.FromNothing.ToHat(currentStage.originInput());
            case NOTHING -> null;
        };
        mappingBuilder.putMapping(mapping);

        delayTillNextStage = 0;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
        inputComponent.confObj().mapping = mappingBuilder.build();
        Controlify.instance().config().save();
    }

    private void goBackStage() {
        int index = stages.indexOf(currentStage);
        int nextIndex = index - 1;
        if (nextIndex >= 0) {
            this.setStage(stages.get(index - 1));
            currentStage.setSatisfied(false);
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

        if (currentStage != null && currentStage.background() != null) {
            guiGraphics.blit(currentStage.background(), 0, 0, 0, 0, 32, 32, 32, 32);
        }

        if (currentStage == null || !currentStage.isSatisfied()) {
            ResourceLocation texture = currentStage != null ? currentStage.foreground() : new ResourceLocation("controlify", "textures/gui/controllerdiagram/faceview.png");
            guiGraphics.blit(texture, 0, 0, 0, 0, 32, 32, 32, 32);
        }

        guiGraphics.setColor(1f, 1f, 1f, 1f);

        guiGraphics.pose().popPose();

        float progress = currentStage != null ? (float) (stages.indexOf(currentStage) + 1) / stages.size() : 0;
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

    public static final class MappingStage {
        private final ResourceLocation originInput;
        private final MapType outputType;
        private final Component name;
        private final ResourceLocation foreground;
        private final ResourceLocation background;
        private boolean satisfied;

        public MappingStage(ResourceLocation originInput, MapType outputType, Component name, String foreground, String background) {
            this.originInput = originInput;
            this.outputType = outputType;
            this.name = name;
            this.foreground = new ResourceLocation("controlify", "textures/gui/controllerdiagram/" + foreground + ".png");;
            this.background = new ResourceLocation("controlify", "textures/gui/controllerdiagram/" + foreground + ".png");;
        }

        public ResourceLocation originInput() {
            return originInput;
        }

        public MapType outputType() {
            return this.outputType;
        }

        public Component name() {
            return name;
        }

        public ResourceLocation foreground() {
            return foreground;
        }

        public ResourceLocation background() {
            return background;
        }

        public boolean isSatisfied() {
            return satisfied;
        }

        public void setSatisfied(boolean satisfied) {
            this.satisfied = satisfied;
        }
    }

    private static class ScreenProcessorImpl extends ScreenProcessor<GamepadEmulationMappingCreatorScreen> {
        public ScreenProcessorImpl(GamepadEmulationMappingCreatorScreen screen) {
            super(screen);
        }

        @Override
        public void onControllerUpdate(ControllerEntity controller) {

        }
    }
}
