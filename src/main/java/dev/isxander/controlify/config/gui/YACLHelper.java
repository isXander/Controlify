package dev.isxander.controlify.config.gui;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.Bind;
import dev.isxander.controlify.config.GlobalSettings;
import dev.isxander.controlify.controller.ControllerTheme;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.yacl.api.*;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.cycling.CyclingListController;
import dev.isxander.yacl.gui.controllers.cycling.EnumController;
import dev.isxander.yacl.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class YACLHelper {
    public static Screen generateConfigScreen(Screen parent) {
        var controlify = Controlify.instance();

        var yacl = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Controlify"))
                .save(() -> controlify.config().save());

        var globalCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.category.global"))
                .option(Option.createBuilder(Controller.class)
                        .name(Component.translatable("controlify.gui.current_controller"))
                        .tooltip(Component.translatable("controlify.gui.current_controller.tooltip"))
                        .binding(Controlify.instance().currentController(), () -> Controlify.instance().currentController(), v -> Controlify.instance().setCurrentController(v))
                        .controller(opt -> new CyclingListController<>(opt, Controller.CONTROLLERS.values().stream().filter(Controller::connected).toList(), c -> Component.literal(c.name())))
                        .instant(true)
                        .build());

        yacl.category(globalCategory.build());

        for (var controller : Controller.CONTROLLERS.values()) {
            var category = ConfigCategory.createBuilder();

            var customName = controller.config().customName;
            category.name(Component.literal(customName == null ? controller.name() : customName));

            var config = controller.config();
            var def = controller.defaultConfig();
            var configGroup = OptionGroup.createBuilder()
                    .name(Component.translatable("controlify.gui.group.config"))
                    .tooltip(Component.translatable("controlify.gui.group.config.tooltip"))
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
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.vmouse_sensitivity"))
                            .tooltip(Component.translatable("controlify.gui.vmouse_sensitivity.tooltip"))
                            .binding(def.virtualMouseSensitivity, () -> config.virtualMouseSensitivity, v -> config.virtualMouseSensitivity = v)
                            .controller(opt -> new FloatSliderController(opt, 0.1f, 2f, 0.05f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build())
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
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.left_trigger_threshold"))
                            .tooltip(Component.translatable("controlify.gui.left_trigger_threshold.tooltip"))
                            .binding(def.leftTriggerActivationThreshold, () -> config.leftTriggerActivationThreshold, v -> config.leftTriggerActivationThreshold = v)
                            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.05f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build())
                    .option(Option.createBuilder(float.class)
                            .name(Component.translatable("controlify.gui.right_trigger_threshold"))
                            .tooltip(Component.translatable("controlify.gui.right_trigger_threshold.tooltip"))
                            .binding(def.rightTriggerActivationThreshold, () -> config.rightTriggerActivationThreshold, v -> config.rightTriggerActivationThreshold = v)
                            .controller(opt -> new FloatSliderController(opt, 0, 1, 0.05f, v -> Component.literal(String.format("%.0f%%", v*100))))
                            .build())
                    .option(Option.createBuilder(ControllerTheme.class)
                            .name(Component.translatable("controlify.gui.controller_theme"))
                            .tooltip(Component.translatable("controlify.gui.controller_theme.tooltip"))
                            .binding(controller.type().theme(), () -> config.theme, v -> config.theme = v)
                            .controller(EnumController::new)
                            .instant(true)
                            .build());
            category.group(configGroup.build());

            var controlsGroup = OptionGroup.createBuilder()
                    .name(Component.translatable("controlify.gui.group.controls"));
            for (var control : controller.bindings().registry().values()) {
                controlsGroup.option(Option.createBuilder(Bind.class)
                        .name(control.name())
                        .binding(control.defaultBind(), control::currentBind, control::setCurrentBind)
                        .controller(opt -> new BindButtonController(opt, controller))
                        .build());
            }
            category.group(controlsGroup.build());

            yacl.category(category.build());
        }

        return yacl.build().generateScreen(parent);
    }
}
