package dev.isxander.controlify.config.gui;

import com.google.common.collect.Iterables;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.config.GlobalSettings;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.controller.gamepad.BuiltinGamepadTheme;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.gui.screen.ControllerDeadzoneCalibrationScreen;
import dev.isxander.yacl.api.*;
import dev.isxander.yacl.gui.controllers.ActionController;
import dev.isxander.yacl.gui.controllers.BooleanController;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.cycling.CyclingListController;
import dev.isxander.yacl.gui.controllers.cycling.EnumController;
import dev.isxander.yacl.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl.gui.controllers.string.StringController;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class YACLHelper {
    public static Screen generateConfigScreen(Screen parent) {
        var controlify = Controlify.instance();

        var yacl = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Controlify"))
                .save(() -> controlify.config().save());

        var globalSettings = Controlify.instance().config().globalSettings();
        var globalCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.category.global"))
                .option(Option.createBuilder((Class<Controller<?, ?>>) (Class<?>) Controller.class)
                        .name(Component.translatable("controlify.gui.current_controller"))
                        .tooltip(Component.translatable("controlify.gui.current_controller.tooltip"))
                        .binding(Controlify.instance().currentController(), () -> Controlify.instance().currentController(), v -> Controlify.instance().setCurrentController(v))
                        .controller(opt -> new CyclingListController<>(opt, Iterables.concat(List.of(Controller.DUMMY), Controller.CONTROLLERS.values().stream().filter(Controller::canBeUsed).toList()), c -> Component.literal(c == Controller.DUMMY ? "Disabled" : c.name())))
                        .instant(true)
                        .build())
                .option(Option.createBuilder(boolean.class)
                        .name(Component.translatable("controlify.gui.out_of_focus_input"))
                        .tooltip(Component.translatable("controlify.gui.out_of_focus_input.tooltip"))
                        .binding(GlobalSettings.DEFAULT.outOfFocusInput, () -> globalSettings.outOfFocusInput, v -> globalSettings.outOfFocusInput = v)
                        .controller(TickBoxController::new)
                        .build())
                .option(Option.createBuilder(boolean.class)
                        .name(Component.translatable("controlify.gui.keyboard_movement"))
                        .tooltip(Component.translatable("controlify.gui.keyboard_movement.tooltip"))
                        .binding(GlobalSettings.DEFAULT.keyboardMovement, () -> globalSettings.keyboardMovement, v -> globalSettings.keyboardMovement = v)
                        .controller(TickBoxController::new)
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(Component.translatable("controlify.gui.open_issue_tracker"))
                        .action((screen, button) -> Util.getPlatform().openUri("https://github.com/isxander/controlify/issues"))
                        .controller(opt -> new ActionController(opt, Component.translatable("controlify.gui.format.open")))
                        .build());

        yacl.category(globalCategory.build());

        for (var controller : Controller.CONTROLLERS.values()) {
            yacl.category(createControllerCategory(controller));
        }

        return yacl.build().generateScreen(parent);
    }

    private static ConfigCategory createControllerCategory(Controller<?, ?> controller) {
        var category = ConfigCategory.createBuilder();

        category.name(Component.literal(controller.name()));

        if (!controller.canBeUsed()) {
            category.tooltip(Component.translatable("controlify.gui.controller_unavailable"));
        }

        var config = controller.config();
        var def = controller.defaultConfig();

        Function<Float, Component> percentFormatter = v -> Component.literal(String.format("%.0f%%", v*100));

        var basicGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.basic"))
                .tooltip(Component.translatable("controlify.gui.group.basic.tooltip"))
                .option(Option.createBuilder(float.class)
                        .name(Component.translatable("controlify.gui.horizontal_look_sensitivity"))
                        .tooltip(Component.translatable("controlify.gui.horizontal_look_sensitivity.tooltip"))
                        .binding(def.horizontalLookSensitivity, () -> config.horizontalLookSensitivity, v -> config.horizontalLookSensitivity = v)
                        .controller(opt -> new FloatSliderController(opt, 0.1f, 2f, 0.05f, percentFormatter))
                        .build())
                .option(Option.createBuilder(float.class)
                        .name(Component.translatable("controlify.gui.vertical_look_sensitivity"))
                        .tooltip(Component.translatable("controlify.gui.vertical_look_sensitivity.tooltip"))
                        .binding(def.verticalLookSensitivity, () -> config.verticalLookSensitivity, v -> config.verticalLookSensitivity = v)
                        .controller(opt -> new FloatSliderController(opt, 0.1f, 2f, 0.05f, percentFormatter))
                        .build())
                .option(Option.createBuilder(boolean.class)
                        .name(Component.translatable("controlify.gui.toggle_sprint"))
                        .tooltip(Component.translatable("controlify.gui.toggle_sprint.tooltip"))
                        .binding(def.toggleSprint, () -> config.toggleSprint, v -> config.toggleSprint = v)
                        .controller(opt -> new BooleanController(opt, v -> Component.translatable("controlify.gui.format.hold_toggle." + (v ? "toggle" : "hold")), false))
                        .build())
                .option(Option.createBuilder(boolean.class)
                        .name(Component.translatable("controlify.gui.toggle_sneak"))
                        .tooltip(Component.translatable("controlify.gui.toggle_sneak.tooltip"))
                        .binding(def.toggleSneak, () -> config.toggleSneak, v -> config.toggleSneak = v)
                        .controller(opt -> new BooleanController(opt, v -> Component.translatable("controlify.gui.format.hold_toggle." + (v ? "toggle" : "hold")), false))
                        .build())
                .option(Option.createBuilder(boolean.class)
                        .name(Component.translatable("controlify.gui.auto_jump"))
                        .tooltip(Component.translatable("controlify.gui.auto_jump.tooltip"))
                        .binding(def.autoJump, () -> config.autoJump, v -> config.autoJump = v)
                        .controller(BooleanController::new)
                        .build())
                .option(Option.createBuilder(boolean.class)
                        .name(Component.translatable("controlify.gui.show_guide"))
                        .tooltip(Component.translatable("controlify.gui.show_guide.tooltip"))
                        .binding(def.showGuide, () -> config.showGuide, v -> config.showGuide = v)
                        .controller(TickBoxController::new)
                        .build())
                .option(Option.createBuilder(float.class)
                        .name(Component.translatable("controlify.gui.vmouse_sensitivity"))
                        .tooltip(Component.translatable("controlify.gui.vmouse_sensitivity.tooltip"))
                        .binding(def.virtualMouseSensitivity, () -> config.virtualMouseSensitivity, v -> config.virtualMouseSensitivity = v)
                        .controller(opt -> new FloatSliderController(opt, 0.1f, 2f, 0.05f, percentFormatter))
                        .build())
                .option(Option.createBuilder(float.class)
                        .name(Component.translatable("controlify.gui.chat_screen_offset"))
                        .tooltip(Component.translatable("controlify.gui.chat_screen_offset.tooltip"))
                        .binding(def.chatKeyboardHeight, () -> config.chatKeyboardHeight, v -> config.chatKeyboardHeight = v)
                        .controller(opt -> new FloatSliderController(opt, 0f, 0.8f, 0.1f, percentFormatter))
                        .build());

        if (controller instanceof GamepadController gamepad) {
            var gamepadConfig = gamepad.config();
            var defaultGamepadConfig = gamepad.defaultConfig();

            basicGroup.option(Option.createBuilder(BuiltinGamepadTheme.class)
                    .name(Component.translatable("controlify.gui.controller_theme"))
                    .tooltip(Component.translatable("controlify.gui.controller_theme.tooltip"))
                    .binding(defaultGamepadConfig.theme, () -> gamepadConfig.theme, v -> gamepadConfig.theme = v)
                    .controller(EnumController::new)
                    .instant(true)
                    .build());
        }

        basicGroup
                .option(Option.createBuilder(String.class)
                        .name(Component.translatable("controlify.gui.custom_name"))
                        .tooltip(Component.translatable("controlify.gui.custom_name.tooltip"))
                        .binding(def.customName == null ? "" : def.customName, () -> config.customName == null ? "" : config.customName, v -> config.customName = (v.equals("") ? null : v))
                        .controller(StringController::new)
                        .build());
        category.group(basicGroup.build());

        var advancedGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.advanced"))
                .tooltip(Component.translatable("controlify.gui.group.advanced.tooltip"))
                .collapsed(true)
                .option(Option.createBuilder(int.class)
                        .name(Component.translatable("controlify.gui.screen_repeat_navi_delay"))
                        .tooltip(Component.translatable("controlify.gui.screen_repeat_navi_delay.tooltip"))
                        .binding(def.screenRepeatNavigationDelay, () -> config.screenRepeatNavigationDelay, v -> config.screenRepeatNavigationDelay = v)
                        .controller(opt -> new IntegerSliderController(opt, 1, 20, 1, v -> Component.translatable("controlify.gui.format.ticks", v)))
                        .build());

        if (controller instanceof GamepadController gamepad) {
            var gpCfg = gamepad.config();
            var gpCfgDef = gamepad.defaultConfig();
            advancedGroup
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.left_stick_deadzone"))
                            .tooltip(Component.translatable("controlify.gui.left_stick_deadzone.tooltip"))
                            .tooltip(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED))
                            .binding(
                                    Math.max(gpCfgDef.leftStickDeadzoneX, gpCfgDef.leftStickDeadzoneY),
                                    () -> Math.max(gpCfg.leftStickDeadzoneX, gpCfgDef.leftStickDeadzoneY),
                                    v -> gpCfg.leftStickDeadzoneX = gpCfg.leftStickDeadzoneY = v
                            )
                            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build())
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.right_stick_deadzone"))
                            .tooltip(Component.translatable("controlify.gui.right_stick_deadzone.tooltip"))
                            .tooltip(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED))
                            .binding(
                                    Math.max(gpCfgDef.rightStickDeadzoneX, gpCfgDef.rightStickDeadzoneY),
                                    () -> Math.max(gpCfg.rightStickDeadzoneX, gpCfgDef.rightStickDeadzoneY),
                                    v -> gpCfg.rightStickDeadzoneX = gpCfg.rightStickDeadzoneY = v
                            )
                            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build());
        } else if (controller instanceof JoystickController joystick) {
            Collection<Integer> deadzoneAxes = IntStream.range(0, joystick.axisCount())
                    .filter(i -> joystick.mapping().axis(i).requiresDeadzone())
                    .boxed()
                    .collect(Collectors.toMap(
                            i -> joystick.mapping().axis(i).identifier(),
                            i -> i,
                            (x, y) -> x,
                            LinkedHashMap::new
                    ))
                    .values();
            var jsCfg = joystick.config();
            var jsCfgDef = joystick.defaultConfig();

            for (int i : deadzoneAxes) {
                advancedGroup.option(Option.createBuilder(float.class)
                        .name(Component.translatable("controlify.gui.joystick_axis_deadzone", joystick.mapping().axis(i).name()))
                        .tooltip(Component.translatable("controlify.gui.joystick_axis_deadzone.tooltip", joystick.mapping().axis(i).name()))
                        .tooltip(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED))
                        .binding(jsCfgDef.getDeadzone(i), () -> jsCfg.getDeadzone(i), v -> jsCfg.setDeadzone(i, v))
                        .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, v -> Component.literal(String.format("%.0f%%", v*100))))
                        .build());
            }
        }

        advancedGroup
                .option(ButtonOption.createBuilder()
                        .name(Component.translatable("controlify.gui.auto_calibration"))
                        .tooltip(Component.translatable("controlify.gui.auto_calibration.tooltip"))
                        .action((screen, button) -> Minecraft.getInstance().setScreen(new ControllerDeadzoneCalibrationScreen(controller, screen)))
                        .controller(ActionController::new)
                        .build())
                .option(Option.createBuilder(float.class)
                        .name(Component.translatable("controlify.gui.button_activation_threshold"))
                        .tooltip(Component.translatable("controlify.gui.button_activation_threshold.tooltip"))
                        .binding(def.buttonActivationThreshold, () -> config.buttonActivationThreshold, v -> config.buttonActivationThreshold = v)
                        .controller(opt -> new FloatSliderController(opt, 0, 1, 0.05f, v -> Component.literal(String.format("%.0f%%", v*100))))
                        .build());
        category.group(advancedGroup.build());

        var controlsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.controls"));
        if (controller instanceof GamepadController gamepad) {
            for (var binding : gamepad.bindings().registry().values()) {
                controlsGroup.option(Option.createBuilder((Class<IBind<GamepadState>>) (Class<?>) IBind.class)
                        .name(binding.name())
                        .binding(binding.defaultBind(), binding::currentBind, binding::setCurrentBind)
                        .controller(opt -> new GamepadBindController(opt, gamepad))
                        .tooltip(binding.description())
                        .build());
            }
        } else if (controller instanceof JoystickController joystick) {
            for (var binding : joystick.bindings().registry().values()) {
                controlsGroup.option(Option.createBuilder((Class<IBind<JoystickState>>) (Class<?>) IBind.class)
                        .name(binding.name())
                        .binding(binding.defaultBind(), binding::currentBind, binding::setCurrentBind)
                        .controller(opt -> new JoystickBindController(opt, joystick))
                        .tooltip(binding.description())
                        .build());
            }
        }

        category.group(controlsGroup.build());

        return category.build();
    }
}
