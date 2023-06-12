package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.EmptyBind;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerConfig;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.joystick.SingleJoystickController;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.gui.controllers.AbstractBindController;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerConfigScreenFactory {
    private static final Function<Float, Component> percentFormatter = v -> Component.literal(String.format("%.0f%%", v*100));
    private static final Function<Float, Component> percentOrOffFormatter = v -> v == 0 ? CommonComponents.OPTION_OFF : percentFormatter.apply(v);

    public static Screen generateConfigScreen(Screen parent, Controller<?, ?> controller) {
        ControllerConfig def = controller.defaultConfig();
        ControllerConfig config = controller.config();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Controlify"))
                .category(createBasicCategory(controller, def, config))
                .category(createAdvancedCategory(controller))
                .category(createBindsCategory(controller))
                .save(() -> Controlify.instance().config().save())
                .build().generateScreen(parent);
    }

    private static ConfigCategory createBasicCategory(Controller<?, ?> controller, ControllerConfig def, ControllerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.config.category.basic"))
                .option(Option.<String>createBuilder()
                        .name(Component.translatable("controlify.gui.custom_name"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.custom_name.tooltip")))
                        .binding(def.customName == null ? "" : def.customName, () -> config.customName == null ? "" : config.customName, v -> config.customName = (v.equals("") ? null : v))
                        .controller(StringControllerBuilder::create)
                        .build())
                .group(makeSensitivityGroup(controller, def, config))
                .group(makeControlsGroup(controller, def, config))
                .group(makeAccessibilityGroup(controller, controller.defaultConfig(), controller.config()))
                .group(makeDeadzoneGroup(controller, controller.defaultConfig(), controller.config()))
                .build();
    }

    private static OptionGroup makeSensitivityGroup(Controller<?, ?> controller, ControllerConfig def, ControllerConfig config) {
        return OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.config.group.sensitivity"))
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.horizontal_look_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.horizontal_look_sensitivity.tooltip"))
                                .build())
                        .binding(def.horizontalLookSensitivity, () -> config.horizontalLookSensitivity, v -> config.horizontalLookSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).valueFormatter(percentFormatter))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.vertical_look_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.vertical_look_sensitivity.tooltip"))
                                .build())
                        .binding(def.verticalLookSensitivity, () -> config.verticalLookSensitivity, v -> config.verticalLookSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).valueFormatter(percentFormatter))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.vmouse_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.vmouse_sensitivity.tooltip"))
                                .build())
                        .binding(def.virtualMouseSensitivity, () -> config.virtualMouseSensitivity, v -> config.virtualMouseSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).valueFormatter(percentFormatter))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.reduce_aiming_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.reduce_aiming_sensitivity.tooltip"))
                                .webpImage(screenshot("reduce-aim-sensitivity.webp"))
                                .build())
                        .binding(def.reduceAimingSensitivity, () -> config.reduceAimingSensitivity, v -> config.reduceAimingSensitivity = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build();
    }

    private static OptionGroup makeControlsGroup(Controller<?, ?> controller, ControllerConfig def, ControllerConfig config) {
        Function<Boolean, Component> holdToggleFormatter = v -> Component.translatable("controlify.gui.format.hold_toggle." + (v ? "toggle" : "hold"));

        return OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.config.group.controls"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.toggle_sprint"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.toggle_sprint.tooltip"))
                                .build())
                        .binding(def.toggleSprint, () -> config.toggleSprint, v -> config.toggleSprint = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .valueFormatter(holdToggleFormatter)
                                .coloured(false))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.toggle_sneak"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.toggle_sneak.tooltip"))
                                .build())
                        .binding(def.toggleSneak, () -> config.toggleSneak, v -> config.toggleSneak = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .valueFormatter(holdToggleFormatter)
                                .coloured(false))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.auto_jump"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.auto_jump.tooltip"))
                                .build())
                        .binding(def.autoJump, () -> config.autoJump, v -> config.autoJump = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .onOffFormatter())
                        .build())
                .build();
    }

    private static OptionGroup makeAccessibilityGroup(Controller<?, ?> controller, ControllerConfig def, ControllerConfig config) {
        return OptionGroup.createBuilder()
                .name(Component.translatable("controlify.config.group.accessibility"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.show_ingame_guide"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.show_ingame_guide.tooltip"))
                                .image(screenshot("ingame-button-guide.png"), 961, 306)
                                .build())
                        .binding(def.showIngameGuide, () -> config.showIngameGuide, v -> config.showIngameGuide = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.show_screen_guide"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.show_screen_guide.tooltip"))
                                .webpImage(screenshot("screen-button-guide.webp"))
                                .build())
                        .binding(def.showScreenGuide, () -> config.showScreenGuide, v -> config.showScreenGuide = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.chat_screen_offset"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.chat_screen_offset.tooltip"))
                                .build())
                        .binding(def.chatKeyboardHeight, () -> config.chatKeyboardHeight, v -> config.chatKeyboardHeight = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0f, 8f).step(0.1f).valueFormatter(percentFormatter))
                        .build())
                .build();
    }

    private static OptionGroup makeDeadzoneGroup(Controller<?, ?> controller, ControllerConfig def, ControllerConfig config) {
        var deadzoneOpts = new ArrayList<Option<Float>>();

        var group = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.config.group.deadzones"));
        if (controller instanceof GamepadController gamepad) {
            var gpCfg = gamepad.config();
            var gpCfgDef = gamepad.defaultConfig();

            Option<Float> left = Option.<Float>createBuilder()
                    .name(Component.translatable("controlify.gui.axis_deadzone", Component.translatable("controlify.gui.left_stick")))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.axis_deadzone.tooltip", Component.translatable("controlify.gui.left_stick")))
                            .text(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED))
                            .build())
                    .binding(
                            gpCfgDef.getLeftStickDeadzone(),
                            gpCfg::getLeftStickDeadzone,
                            gpCfg::setLeftStickDeadzone
                    )
                    .controller(opt -> FloatSliderControllerBuilder.create(opt)
                            .range(0f, 1f).step(0.01f)
                            .valueFormatter(percentFormatter))
                    .build();

            Option<Float> right = Option.<Float>createBuilder()
                    .name(Component.translatable("controlify.gui.axis_deadzone", Component.translatable("controlify.gui.right_stick")))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.axis_deadzone.tooltip", Component.translatable("controlify.gui.right_stick")))
                            .text(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED))
                            .build())
                    .binding(
                            gpCfgDef.getRightStickDeadzone(),
                            gpCfg::getRightStickDeadzone,
                            gpCfg::setRightStickDeadzone
                    )
                    .controller(opt -> FloatSliderControllerBuilder.create(opt)
                            .range(0f, 1f).step(0.01f)
                            .valueFormatter(percentFormatter))
                    .build();

            group.option(left);
            group.option(right);

            deadzoneOpts.add(left);
            deadzoneOpts.add(right);
        } else if (controller instanceof SingleJoystickController joystick) {
            JoystickMapping.Axis[] axes = joystick.mapping().axes();
            Collection<Integer> deadzoneAxes = IntStream.range(0, axes.length)
                    .filter(i -> axes[i].requiresDeadzone())
                    .boxed()
                    .collect(Collectors.toMap(
                            i -> axes[i].identifier(),
                            i -> i,
                            (x, y) -> x,
                            LinkedHashMap::new
                    ))
                    .values();
            var jsCfg = joystick.config();
            var jsCfgDef = joystick.defaultConfig();

            for (int i : deadzoneAxes) {
                var axis = axes[i];

                Option<Float> deadzoneOpt = Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.joystick_axis_deadzone", axis.name()))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.joystick_axis_deadzone.tooltip", axis.name()))
                                .text(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED))
                                .build())
                        .binding(jsCfgDef.getDeadzone(i), () -> jsCfg.getDeadzone(i), v -> jsCfg.setDeadzone(i, v))
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0f, 1f).step(0.01f)
                                .valueFormatter(percentFormatter))
                        .build();
                group.option(deadzoneOpt);
                deadzoneOpts.add(deadzoneOpt);
            }
        }

        group.option(Option.<Float>createBuilder()
                .name(Component.translatable("controlify.gui.button_activation_threshold"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.button_activation_threshold.tooltip"))
                        .build())
                .binding(def.buttonActivationThreshold, () -> config.buttonActivationThreshold, v -> config.buttonActivationThreshold = v)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(0f, 1f).step(0.01f)
                        .valueFormatter(percentFormatter))
                .build());

        group.option(ButtonOption.createBuilder()
                .name(Component.translatable("controlify.gui.auto_calibration"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.auto_calibration.tooltip"))
                        .build())
                .action((screen, button) -> Minecraft.getInstance().setScreen(new ControllerCalibrationScreen(controller, () -> {
                    deadzoneOpts.forEach(Option::forgetPendingValue);
                    return screen;
                })))
                .build());

        return group.build();
    }

    private static ConfigCategory createAdvancedCategory(Controller<?, ?> controller) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.config.category.advanced"))
                .group(makeVibrationGroup(controller))
                .group(makeGyroGroup(controller))
                .build();
    }

    private static ConfigCategory createBindsCategory(Controller<?, ?> controller) {
        var category = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.group.controls"));

        List<OptionBindPair> optionBinds = new ArrayList<>();
        groupBindings(controller.bindings().registry().values()).forEach((categoryName, bindGroup) -> {
            var controlsGroup = OptionGroup.createBuilder()
                    .name(categoryName);

            controlsGroup.options(bindGroup.stream().map(binding -> {
                Option.Builder<?> option = binding.startYACLOption()
                        .listener((opt, val) -> updateConflictingBinds(optionBinds));

                Option<?> built = option.build();
                optionBinds.add(new OptionBindPair(built, binding));
                return built;
            }).toList());

            category.group(controlsGroup.build());
        });
        updateConflictingBinds(optionBinds);

        return category.build();
    }

    private static void updateConflictingBinds(List<OptionBindPair> all) {
        all.forEach(pair -> ((AbstractBindController<?>) pair.option().controller()).setConflicting(false));

        for (OptionBindPair opt : all) {
            var ctxs = BindContext.flatten(opt.binding().contexts());

            List<OptionBindPair> conflicting = all.stream()
                    .filter(pair -> pair.binding() != opt.binding())
                    .filter(pair -> {
                        boolean contextsMatch = BindContext.flatten(pair.binding().contexts())
                                .stream()
                                .anyMatch(ctxs::contains);
                        boolean bindMatches = pair.option().pendingValue().equals(opt.option().pendingValue());
                        boolean bindIsNotEmpty = !(pair.option().pendingValue() instanceof EmptyBind<?>);
                        return contextsMatch && bindMatches && bindIsNotEmpty;
                    }).toList();

            conflicting.forEach(conflict -> ((AbstractBindController<?>) conflict.option().controller()).setConflicting(true));
        }
    }

    private static OptionGroup makeVibrationGroup(Controller<?, ?> controller) {
        boolean canRumble = controller.supportsRumble();
        var config = controller.config();
        var def = controller.defaultConfig();

        var vibrationGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.vibration"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.group.vibration.tooltip"))
                        .build());
        if (canRumble) {
            List<Option<Float>> strengthOptions = new ArrayList<>();
            Option<Boolean> allowVibrationOption;
            vibrationGroup.option(allowVibrationOption = Option.<Boolean>createBuilder()
                    .name(Component.translatable("controlify.gui.allow_vibrations"))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.allow_vibrations.tooltip"))
                            .build())
                    .binding(def.allowVibrations, () -> config.allowVibrations, v -> config.allowVibrations = v)
                    .listener((opt, allowVibration) -> strengthOptions.forEach(so -> so.setAvailable(allowVibration)))
                    .controller(TickBoxControllerBuilder::create)
                    .build());
            for (RumbleSource source : RumbleSource.values()) {
                var option = Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.vibration_strength." + source.id().getNamespace() + "." + source.id().getPath()))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.vibration_strength." + source.id().getNamespace() + "." + source.id().getPath() + ".tooltip"))
                                .build())
                        .binding(
                                def.getRumbleStrength(source),
                                () -> config.getRumbleStrength(source),
                                v -> config.setRumbleStrength(source, v)
                        )
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0f, 2f)
                                .step(0.05f)
                                .valueFormatter(percentOrOffFormatter))
                        .available(allowVibrationOption.pendingValue())
                        .build();
                strengthOptions.add(option);
                vibrationGroup.option(option);
            }
            vibrationGroup.option(ButtonOption.createBuilder()
                    .name(Component.translatable("controlify.gui.test_vibration"))
                    .description(OptionDescription.of(Component.translatable("controlify.gui.test_vibration.tooltip")))
                    .action((screen, btn) -> {
                        controller.rumbleManager().play(
                                RumbleSource.MASTER,
                                BasicRumbleEffect.byTime(t -> new RumbleState(0f, t), 20)
                                        .join(BasicRumbleEffect.byTime(t -> new RumbleState(0f, 1 - t), 20))
                                        .repeat(3)
                                        .join(BasicRumbleEffect.constant(1f, 0f, 5)
                                                .join(BasicRumbleEffect.constant(0f, 1f, 5))
                                                .repeat(10)
                                        )
                                        .earlyFinish(BasicRumbleEffect.finishOnScreenChange())
                        );
                    })
                    .build());
        } else {
            vibrationGroup.option(LabelOption.create(Component.translatable("controlify.gui.allow_vibrations.not_available").withStyle(ChatFormatting.RED)));
        }

        return vibrationGroup.build();
    }

    private static OptionGroup makeGyroGroup(Controller<?, ?> controller) {
        GamepadController gamepad = (controller instanceof GamepadController) ? (GamepadController) controller : null;
        boolean hasGyro = gamepad != null && gamepad.hasGyro();

        var gpCfg = gamepad != null ? gamepad.config() : null;
        var gpCfgDef = gamepad != null ? gamepad.defaultConfig() : null;

        Option<Float> gyroSensitivity;
        List<Option<?>> gyroOptions = new ArrayList<>();
        var gyroGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.gyro"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.group.gyro.tooltip"))
                        .build())
                .collapsed(!hasGyro);
        if (hasGyro) {
            gyroGroup.option(gyroSensitivity = Option.<Float>createBuilder()
                    .name(Component.translatable("controlify.gui.gyro_look_sensitivity"))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.gyro_look_sensitivity.tooltip"))
                            .build())
                    .binding(gpCfgDef.gyroLookSensitivity, () -> gpCfg.gyroLookSensitivity, v -> gpCfg.gyroLookSensitivity = v)
                    .controller(opt -> FloatSliderControllerBuilder.create(opt)
                            .range(0f, 1f)
                            .step(0.05f)
                            .valueFormatter(percentOrOffFormatter))
                    .listener((opt, sensitivity) -> gyroOptions.forEach(o -> {
                        o.setAvailable(sensitivity > 0);
                        o.requestSetDefault();
                    }))
                    .build());
            var relativeModeOpt = Option.<Boolean>createBuilder()
                    .name(Component.translatable("controlify.gui.gyro_behaviour"))
                    .description(val -> OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.gyro_behaviour.tooltip"))
                            .text(val ? Component.translatable("controlify.gui.gyro_behaviour.relative.tooltip") : Component.translatable("controlify.gui.gyro_behaviour.absolute.tooltip"))
                            .build())
                    .binding(gpCfgDef.relativeGyroMode, () -> gpCfg.relativeGyroMode, v -> gpCfg.relativeGyroMode = v)
                    .controller(opt -> BooleanControllerBuilder.create(opt)
                            .valueFormatter(v -> v ? Component.translatable("controlify.gui.gyro_behaviour.relative") : Component.translatable("controlify.gui.gyro_behaviour.absolute")))
                    .build();
            gyroGroup.option(relativeModeOpt);
            gyroGroup.option(Util.make(() -> {
                var opt = Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.gyro_invert_x"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.gyro_invert_x.tooltip")))
                        .binding(gpCfgDef.invertGyroX, () -> gpCfg.invertGyroX, v -> gpCfg.invertGyroX = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build();
                gyroOptions.add(opt);
                return opt;
            }));
            gyroGroup.option(Util.make(() -> {
                var opt = Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.gyro_invert_y"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.gyro_invert_y.tooltip")))
                        .binding(gpCfgDef.invertGyroY, () -> gpCfg.invertGyroY, v -> gpCfg.invertGyroY = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build();
                gyroOptions.add(opt);
                return opt;
            }));
            gyroGroup.option(Util.make(() -> {
                var opt = Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.gyro_requires_button"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.gyro_requires_button.tooltip"))
                                .build())
                        .binding(gpCfgDef.gyroRequiresButton, () -> gpCfg.gyroRequiresButton, v -> gpCfg.gyroRequiresButton = v)
                        .controller(TickBoxControllerBuilder::create)
                        .available(gyroSensitivity.pendingValue() > 0)
                        .listener((o, val) -> {
                            if (val) {
                                relativeModeOpt.setAvailable(gyroSensitivity.pendingValue() > 0);
                            } else {
                                relativeModeOpt.setAvailable(false);
                                relativeModeOpt.requestSet(false);
                            }
                        })
                        .build();
                gyroOptions.add(opt);
                return opt;
            }));
            gyroGroup.option(Util.make(() -> {
                var opt = Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.flick_stick"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.flick_stick.tooltip"))
                                .build())
                        .binding(gpCfgDef.flickStick, () -> gpCfg.flickStick, v -> gpCfg.flickStick = v)
                        .controller(TickBoxControllerBuilder::create)
                        .available(gyroSensitivity.pendingValue() > 0)
                        .build();
                gyroOptions.add(opt);
                return opt;
            }));
        } else {
            gyroGroup.option(LabelOption.create(Component.translatable("controlify.gui.group.gyro.no_gyro.tooltip").withStyle(ChatFormatting.RED)));
        }

        return gyroGroup.build();
    }

    private static Map<Component, List<ControllerBinding>> groupBindings(Collection<ControllerBinding> bindings) {
        return bindings.stream()
                .collect(Collectors.groupingBy(ControllerBinding::category, LinkedHashMap::new, Collectors.toList()));
    }

    private static ResourceLocation screenshot(String filename) {
        return Controlify.id("textures/screenshots/" + filename);
    }

    private record OptionBindPair(Option<?> option, ControllerBinding binding) {
    }
}
