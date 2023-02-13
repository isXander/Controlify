package dev.isxander.controlify.config.gui;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.config.GlobalSettings;
import dev.isxander.controlify.controller.ControllerTheme;
import dev.isxander.controlify.controller.Controller;
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
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class YACLHelper {
    public static Screen generateConfigScreen(Screen parent) {
        if (Controlify.instance().currentController() == null) {
            return new AlertScreen(
                    () -> Minecraft.getInstance().setScreen(parent),
                    Component.translatable("controlify.gui.error.title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    Component.translatable("controlify.gui.error.message").withStyle(ChatFormatting.RED)
            );
        }

        var controlify = Controlify.instance();

        var yacl = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Controlify"))
                .save(() -> controlify.config().save());

        var globalSettings = Controlify.instance().config().globalSettings();
        var globalCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.category.global"))
                .option(Option.createBuilder(Controller.class)
                        .name(Component.translatable("controlify.gui.current_controller"))
                        .tooltip(Component.translatable("controlify.gui.current_controller.tooltip"))
                        .binding(Controlify.instance().currentController(), () -> Controlify.instance().currentController(), v -> Controlify.instance().setCurrentController(v))
                        .controller(opt -> new CyclingListController<>(opt, Controller.CONTROLLERS.values().stream().filter(Controller::connected).toList(), c -> Component.literal(c.name())))
                        .instant(true)
                        .build())
                .option(Option.createBuilder(boolean.class)
                        .name(Component.translatable("controlify.gui.out_of_focus_input"))
                        .tooltip(Component.translatable("controlify.gui.out_of_focus_input.tooltip"))
                        .binding(GlobalSettings.DEFAULT.outOfFocusInput, () -> globalSettings.outOfFocusInput, v -> globalSettings.outOfFocusInput = v)
                        .controller(TickBoxController::new)
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(Component.translatable("controlify.gui.open_issue_tracker"))
                        .action((screen, button) -> Util.getPlatform().openUri("https://github.com/isxander/controlify/issues"))
                        .controller(opt -> new ActionController(opt, Component.translatable("controlify.gui.format.open")))
                        .build());

        yacl.category(globalCategory.build());

        for (var controller : Controller.CONTROLLERS.values()) {
            var category = ConfigCategory.createBuilder();

            category.name(Component.literal(controller.name()));

            var config = controller.config();
            var def = controller.defaultConfig();

            var basicGroup = OptionGroup.createBuilder()
                    .name(Component.translatable("controlify.gui.group.basic"))
                    .tooltip(Component.translatable("controlify.gui.group.basic.tooltip"))
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.horizontal_look_sensitivity"))
                            .tooltip(Component.translatable("controlify.gui.horizontal_look_sensitivity.tooltip"))
                            .binding(def.horizontalLookSensitivity, () -> config.horizontalLookSensitivity, v -> config.horizontalLookSensitivity = v)
                            .controller(opt -> new FloatSliderController(opt, 0.1f, 2f, 0.05f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build())
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.vertical_look_sensitivity"))
                            .tooltip(Component.translatable("controlify.gui.vertical_look_sensitivity.tooltip"))
                            .binding(def.verticalLookSensitivity, () -> config.verticalLookSensitivity, v -> config.verticalLookSensitivity = v)
                            .controller(opt -> new FloatSliderController(opt, 0.1f, 2f, 0.05f, v -> Component.literal(String.format("%.0f%%", v*100))))
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
                            .controller(opt -> new FloatSliderController(opt, 0.1f, 2f, 0.05f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build())
                    .option(Option.createBuilder(ControllerTheme.class)
                            .name(Component.translatable("controlify.gui.controller_theme"))
                            .tooltip(Component.translatable("controlify.gui.controller_theme.tooltip"))
                            .binding(controller.type().theme(), () -> config.theme, v -> config.theme = v)
                            .controller(EnumController::new)
                            .instant(true)
                            .build())
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
                            .build())
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.left_stick_deadzone"))
                            .tooltip(Component.translatable("controlify.gui.left_stick_deadzone.tooltip"))
                            .tooltip(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED))
                            .binding(def.leftStickDeadzone, () -> config.leftStickDeadzone, v -> config.leftStickDeadzone = v)
                            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build())
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.right_stick_deadzone"))
                            .tooltip(Component.translatable("controlify.gui.right_stick_deadzone.tooltip"))
                            .tooltip(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED))
                            .binding(def.rightStickDeadzone, () -> config.rightStickDeadzone, v -> config.rightStickDeadzone = v)
                            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.01f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build())
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
            for (var control : controller.bindings().registry().values()) {
                controlsGroup.option(Option.createBuilder(IBind.class)
                        .name(control.name())
                        .binding(control.defaultBind(), control::currentBind, control::setCurrentBind)
                        .controller(opt -> new BindButtonController(opt, controller))
                        .tooltip(control.description())
                        .instant(true)
                        .listener((opt, bind) -> { // yacl instant options have a bug where they don't save
                            opt.applyValue();
                            controlify.config().save();
                        })
                        .build());
            }
            category.group(controlsGroup.build());

            yacl.category(category.build());
        }

        return yacl.build().generateScreen(parent);
    }
}
