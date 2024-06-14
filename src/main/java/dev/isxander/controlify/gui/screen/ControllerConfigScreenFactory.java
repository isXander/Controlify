package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.bindings.input.EmptyInput;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.gyro.GyroYawMode;
import dev.isxander.controlify.controller.input.DeadzoneGroup;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.input.Inputs;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.gui.controllers.BindController;
import dev.isxander.controlify.gui.controllers.Deadzone2DImageRenderer;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.server.ServerPolicies;
import dev.isxander.controlify.server.ServerPolicy;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControllerConfigScreenFactory {
    private static final ValueFormatter<Float> percentFormatter = v -> Component.literal(String.format("%.0f%%", v*100));
    private static final ValueFormatter<Float> percentOrOffFormatter = v -> v == 0 ? CommonComponents.OPTION_OFF : percentFormatter.format(v);
    private static final Component newOptionLabel = Component.translatable("controlify.gui.new_options.label").withStyle(ChatFormatting.GOLD);
    private static final ValueFormatter<Integer> ticksToMillisFormatter = v -> Component.literal(String.format("%03dms", v * 50));

    private final List<Option<?>> newOptions = new ArrayList<>();

    public static Screen generateConfigScreen(Screen parent, ControllerEntity controller) {
        return new ControllerConfigScreenFactory().generateConfigScreen0(parent, controller);
    }

    private Screen generateConfigScreen0(Screen parent, ControllerEntity controller) {
        var advancedCategory = createAdvancedCategory(controller);
        var bindsCategory = makeBindsCategory(controller);
        var basicCategory = createBasicCategory(controller); // must be last for new options

        var yacl = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Controlify"))
                .category(basicCategory)
                .category(advancedCategory)
                .save(() -> Controlify.instance().config().save());

        bindsCategory.ifPresent(yacl::category);

        return yacl.build().generateScreen(parent);
    }

    private ConfigCategory createBasicCategory(ControllerEntity controller) {
        var sensitivityGroup = makeSensitivityGroup(controller);
        var controlsGroup = makeControlsGroup(controller);
        var accessibilityGroup = makeAccessibilityGroup(controller);
        var deadzoneGroup = makeDeadzoneGroup(controller);

        GenericControllerConfig config = controller.genericConfig().config();
        GenericControllerConfig def = controller.genericConfig().defaultConfig();

        ConfigCategory.Builder builder = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.config.category.basic"))
                .option(Option.<String>createBuilder()
                        .name(Component.translatable("controlify.gui.custom_name"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.custom_name.tooltip")))
                        .binding(def.nickname == null ? "" : def.nickname, () -> config.nickname == null ? "" : config.nickname, v -> config.nickname = (v.isEmpty() ? null : v))
                        .controller(StringControllerBuilder::create)
                        .build());
        if (!newOptions.isEmpty()) {
            builder.group(OptionGroup.createBuilder()
                    .name(Component.translatable("controlify.gui.new_options").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .description(OptionDescription.of(Component.translatable("controlify.gui.new_options.tooltip")))
                    .options(newOptions)
                    .build());
        }

        sensitivityGroup.ifPresent(builder::group);
        controlsGroup.ifPresent(builder::group);
        accessibilityGroup.ifPresent(builder::group);
        deadzoneGroup.ifPresent(builder::group);

        return builder.build();
    }

    private Optional<OptionGroup> makeSensitivityGroup(ControllerEntity controller) {
        Optional<InputComponent> inputOpt = controller.input();
        if (inputOpt.isEmpty())
            return Optional.empty();

        InputComponent.Config config = inputOpt.get().confObj();
        InputComponent.Config def = inputOpt.get().defObj();

        return Optional.of(OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.config.group.sensitivity"))
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.horizontal_look_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.horizontal_look_sensitivity.tooltip"))
                                .build())
                        .binding(def.hLookSensitivity, () -> config.hLookSensitivity, v -> config.hLookSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).formatValue(percentFormatter))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.vertical_look_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.vertical_look_sensitivity.tooltip"))
                                .build())
                        .binding(def.vLookSensitivity, () -> config.vLookSensitivity, v -> config.vLookSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).formatValue(percentFormatter))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.vmouse_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.vmouse_sensitivity.tooltip"))
                                .build())
                        .binding(def.virtualMouseSensitivity, () -> config.virtualMouseSensitivity, v -> config.virtualMouseSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).formatValue(percentFormatter))
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
                .build());
    }

    private Optional<OptionGroup> makeControlsGroup(ControllerEntity controller) {
        ValueFormatter<Boolean> holdToggleFormatter = v -> Component.translatable("controlify.gui.format.hold_toggle." + (v ? "toggle" : "hold"));

        GenericControllerConfig config = controller.genericConfig().config();
        GenericControllerConfig def = controller.genericConfig().defaultConfig();

        return Optional.of(OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.config.group.controls"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.toggle_sprint"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.toggle_sprint.tooltip"))
                                .build())
                        .binding(def.toggleSprint, () -> config.toggleSprint, v -> config.toggleSprint = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .formatValue(holdToggleFormatter)
                                .coloured(false))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.toggle_sneak"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.toggle_sneak.tooltip"))
                                .build())
                        .binding(def.toggleSneak, () -> config.toggleSneak, v -> config.toggleSneak = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .formatValue(holdToggleFormatter)
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
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.no_fly_drifting"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.no_fly_drifting.tooltip"))
                                .text(ServerPolicies.DISABLE_FLY_DRIFTING.get() != ServerPolicy.UNSET ? Component.translatable("controlify.gui.server_controlled").withStyle(ChatFormatting.GOLD) : Component.empty())
                                .build())
                        .binding(def.disableFlyDrifting, () -> ServerPolicies.DISABLE_FLY_DRIFTING.get().isAllowed() && config.disableFlyDrifting, v -> config.disableFlyDrifting = v)
                        .controller(TickBoxControllerBuilder::create)
                        .available(ServerPolicies.DISABLE_FLY_DRIFTING.get().isAllowed())
                        .build())
                .build());
    }

    private Optional<OptionGroup> makeAccessibilityGroup(ControllerEntity controller) {
        GenericControllerConfig config = controller.genericConfig().config();
        GenericControllerConfig def = controller.genericConfig().defaultConfig();

        return Optional.of(OptionGroup.createBuilder()
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
                        .name(Component.translatable("controlify.gui.ingame_button_guide_position"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.ingame_button_guide_position.tooltip")))
                        .binding(def.ingameGuideBottom, () -> config.ingameGuideBottom, v -> config.ingameGuideBottom = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .formatValue(v -> Component.translatable(v ? "controlify.gui.format.bottom" : "controlify.gui.format.top")))
                        .flag(mc -> Controlify.instance().inGameButtonGuide().ifPresent(InGameButtonGuide::refreshLayout))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.show_screen_guide"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.show_screen_guide.tooltip"))
                                .webpImage(screenshot("screen-button-guide.webp"))
                                .build())
                        .binding(def.showScreenGuides, () -> config.showScreenGuides, v -> config.showScreenGuides = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.show_keyboard"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.show_keyboard.tooltip"))
                                .build())
                        .binding(def.showOnScreenKeyboard, () -> config.showOnScreenKeyboard, v -> config.showOnScreenKeyboard = v)
                        .available(def.showOnScreenKeyboard) // default is if the OS supports it
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build());
    }

    private Optional<OptionGroup> makeDeadzoneGroup(ControllerEntity controller) {
        Optional<InputComponent> inputOpt = controller.input();
        if (inputOpt.isEmpty())
            return Optional.empty();

        InputComponent input = inputOpt.get();
        InputComponent.Config config = input.confObj();
        InputComponent.Config def = input.defObj();

        var deadzoneOpts = new ArrayList<Option<Float>>();

        var group = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.config.group.deadzones"));

        group.option(LabelOption.create(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED)));

        for (DeadzoneGroup deadzoneGroup : input.getDeadzoneGroups().values()) {
            ResourceLocation groupName = deadzoneGroup.name();
            Component name = Component.translatable("controlify.deadzone_group." + groupName.getNamespace() + "." + groupName.getPath());

            AtomicReference<Option<Float>> deadzoneRef = new AtomicReference<>();
            Option<Float> deadzoneOpt = Option.<Float>createBuilder()
                    .name(name)
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.axis_deadzone.tooltip", name))
                            .customImage(CompletableFuture.completedFuture(deadzoneGroup.axes().size() == 4 ? Optional.of(new Deadzone2DImageRenderer(input, deadzoneGroup, deadzoneRef::get)) : Optional.empty()))
                            .build())
                    .binding(
                            def.deadzones.getOrDefault(groupName, 0f),
                            () -> config.deadzones.getOrDefault(groupName, 0f),
                            v -> config.deadzones.put(groupName, v)
                    )
                    .controller(opt -> FloatSliderControllerBuilder.create(opt)
                            .range(0f, 1f).step(0.02f)
                            .formatValue(percentFormatter))
                    .build();
            deadzoneRef.set(deadzoneOpt);
            group.option(deadzoneOpt);
            deadzoneOpts.add(deadzoneOpt);
        }

        group.option(Option.<Float>createBuilder()
                .name(Component.translatable("controlify.gui.button_activation_threshold"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.button_activation_threshold.tooltip"))
                        .build())
                .binding(def.buttonActivationThreshold, () -> config.buttonActivationThreshold, v -> config.buttonActivationThreshold = v)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(0f, 1f).step(0.01f)
                        .formatValue(percentFormatter))
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

        return Optional.of(group.build());
    }

    private ConfigCategory createAdvancedCategory(ControllerEntity controller) {
        Optional<InputComponent> input = controller.input();
        
        ConfigCategory.Builder builder = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.config.category.advanced"));

        input.ifPresent(inputComponent -> builder.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("controlify.gui.mixed_input"))
                .description(OptionDescription.of(Component.translatable("controlify.gui.mixed_input.tooltip")))
                .binding(inputComponent.defObj().mixedInput, () -> inputComponent.confObj().mixedInput, v -> inputComponent.confObj().mixedInput = v)
                .controller(TickBoxControllerBuilder::create)
                .build()));

        makeVibrationGroup(controller).ifPresent(builder::group);
        makeGyroGroup(controller).ifPresent(builder::group);
        makeControllerMappingGroup(controller).ifPresent(builder::group);

        return builder.build();
    }

    private Optional<OptionGroup> makeControllerMappingGroup(ControllerEntity controller) {
        Optional<InputComponent> inputOpt = controller.input();
        if (inputOpt.isEmpty())
            return Optional.empty();
        InputComponent input = inputOpt.get();
        InputComponent.Config config = input.confObj();
        InputComponent.Config def = input.defObj();

        return Optional.of(OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.controller_mapping"))
                .option(LabelOption.create(Component.translatable("controlify.gui.controller_mapping.explanation")))
                .option(ButtonOption.createBuilder()
                        .name(Component.translatable("controlify.gui.create_gamepad_mapping"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.create_gamepad_mapping.tooltip")))
                        .action((screen, button) -> Minecraft.getInstance().setScreen(ControllerMappingMakerScreen.createGamepadMapping(input, screen)))
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(Component.translatable("controlify.gui.clear_mapping"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.clear_mapping.tooltip")))
                        .action((screen, button) -> config.mapping = def.mapping)
                        .build())
                .collapsed(true)
                .build());
    }

    private Optional<OptionGroup> makeVibrationGroup(ControllerEntity controller) {
        var vibrationGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.vibration"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.group.vibration.tooltip"))
                        .build());

        Optional<RumbleComponent> rumbleOpt = controller.rumble();
        if (rumbleOpt.isEmpty()) {
            vibrationGroup.option(LabelOption.create(Component.translatable("controlify.gui.allow_vibrations.not_available").withStyle(ChatFormatting.RED)));
            return Optional.of(vibrationGroup.build());
        }

        RumbleComponent rumble = rumbleOpt.get();

        RumbleComponent.Config config = rumble.confObj();
        RumbleComponent.Config def = rumble.defObj();

        List<Option<Float>> strengthOptions = new ArrayList<>();
        Option<Boolean> allowVibrationOption;
        vibrationGroup.option(allowVibrationOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("controlify.gui.allow_vibrations"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.allow_vibrations.tooltip"))
                        .build())
                .binding(def.enabled, () -> config.enabled, v -> config.enabled = v)
                .listener((opt, allowVibration) -> strengthOptions.forEach(so -> so.setAvailable(allowVibration)))
                .controller(TickBoxControllerBuilder::create)
                .build());

        controller.hdHaptics().ifPresent(haptics -> {
            vibrationGroup.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("controlify.gui.hd_haptics"))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.hd_haptics.tooltip"))
                            .build())
                    .binding(haptics.defObj().enabled, () -> haptics.confObj().enabled, v -> haptics.confObj().enabled = v)
                    .controller(TickBoxControllerBuilder::create)
                    .build());
        });

        for (RumbleSource source : RumbleSource.values()) {
            var option = Option.<Float>createBuilder()
                    .name(Component.translatable("controlify.vibration_strength." + source.id().getNamespace() + "." + source.id().getPath()))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.vibration_strength." + source.id().getNamespace() + "." + source.id().getPath() + ".tooltip"))
                            .build())
                    .binding(
                            def.vibrationStrengths.getOrDefault(source.id(), 1f),
                            () -> config.vibrationStrengths.getOrDefault(source.id(), 1f),
                            v -> config.vibrationStrengths.put(source.id(), v)
                    )
                    .controller(opt -> FloatSliderControllerBuilder.create(opt)
                            .range(0f, 2f)
                            .step(0.05f)
                            .formatValue(percentOrOffFormatter))
                    .available(allowVibrationOption.pendingValue())
                    .build();
            strengthOptions.add(option);
            vibrationGroup.option(option);
        }
        vibrationGroup.option(ButtonOption.createBuilder()
                .name(Component.translatable("controlify.gui.test_vibration"))
                .description(OptionDescription.of(Component.translatable("controlify.gui.test_vibration.tooltip")))
                .action((screen, btn) -> {
                    rumble.rumbleManager().play(
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

        return Optional.of(vibrationGroup.build());
    }

    private Optional<OptionGroup> makeGyroGroup(ControllerEntity controller) {
        var gyroGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.gyro"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.group.gyro.tooltip"))
                        .build());

        Optional<GyroComponent> gyroOpt = controller.gyro();
        if (gyroOpt.isEmpty()) {
            gyroGroup.collapsed(true);
            gyroGroup.option(LabelOption.create(Component.translatable("controlify.gui.group.gyro.no_gyro.tooltip").withStyle(ChatFormatting.RED)));
            return Optional.of(gyroGroup.build());
        }

        GyroComponent.Config config = gyroOpt.get().confObj();
        GyroComponent.Config def = gyroOpt.get().defObj();

        Option<Float> gyroSensitivity;
        List<Option<?>> gyroOptions = new ArrayList<>();
        gyroGroup.option(gyroSensitivity = Option.<Float>createBuilder()
                .name(Component.translatable("controlify.gui.gyro_look_sensitivity"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.gyro_look_sensitivity.tooltip"))
                        .build())
                .binding(def.lookSensitivity, () -> config.lookSensitivity, v -> config.lookSensitivity = v)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(0f, 3f)
                        .step(0.1f)
                        .formatValue(percentOrOffFormatter))
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
                .binding(def.relativeGyroMode, () -> config.relativeGyroMode, v -> config.relativeGyroMode = v)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(v -> v ? Component.translatable("controlify.gui.gyro_behaviour.relative") : Component.translatable("controlify.gui.gyro_behaviour.absolute")))
                .build();
        gyroGroup.option(relativeModeOpt);
        gyroGroup.option(Util.make(() -> {
            var option = Option.<GyroYawMode>createBuilder()
                    .name(Component.translatable("controlify.gui.gyro_yaw_mode"))
                    .description(val -> OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.gyro_yaw_mode.tooltip"))
                            .text(val == GyroYawMode.YAW ? Component.translatable("controlify.gui.gyro_yaw_mode.tooltip.yaw_only") : Component.empty())
                            .text(val == GyroYawMode.ROLL ? Component.translatable("controlify.gui.gyro_yaw_mode.tooltip.roll_only") : Component.empty())
                            .text(val == GyroYawMode.BOTH ? Component.translatable("controlify.gui.gyro_yaw_mode.tooltip.both") : Component.empty())
                            .build())
                    .binding(def.yawMode, () -> config.yawMode, v -> config.yawMode = v)
                    .controller(opt -> EnumControllerBuilder.create(opt).enumClass(GyroYawMode.class))
                    .build();
            gyroOptions.add(option);
            return option;
        }));
        gyroGroup.option(Util.make(() -> {
            var opt = Option.<Boolean>createBuilder()
                    .name(Component.translatable("controlify.gui.gyro_invert_x"))
                    .description(OptionDescription.of(Component.translatable("controlify.gui.gyro_invert_x.tooltip")))
                    .binding(def.invertX, () -> config.invertX, v -> config.invertX = v)
                    .controller(TickBoxControllerBuilder::create)
                    .build();
            gyroOptions.add(opt);
            return opt;
        }));
        gyroGroup.option(Util.make(() -> {
            var opt = Option.<Boolean>createBuilder()
                    .name(Component.translatable("controlify.gui.gyro_invert_y"))
                    .description(OptionDescription.of(Component.translatable("controlify.gui.gyro_invert_y.tooltip")))
                    .binding(def.invertY, () -> config.invertY, v -> config.invertY = v)
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
                    .binding(def.requiresButton, () -> config.requiresButton, v -> config.requiresButton = v)
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
                    .binding(def.flickStick, () -> config.flickStick, v -> config.flickStick = v)
                    .controller(TickBoxControllerBuilder::create)
                    .available(gyroSensitivity.pendingValue() > 0)
                    .build();
            gyroOptions.add(opt);
            return opt;
        }));

        return Optional.of(gyroGroup.build());
    }

    private Optional<ConfigCategory> makeBindsCategory(ControllerEntity controller) {
        Optional<InputComponent> inputOpt = controller.input();
        if (inputOpt.isEmpty())
            return Optional.empty();
        InputComponent input = inputOpt.get();

        var category = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.group.controls"));

        InputComponent.Config config = input.confObj();
        InputComponent.Config def = input.defObj();

        List<OptionBindPair> optionBinds = new ArrayList<>();

        ButtonOption editRadialButton = ButtonOption.createBuilder()
                .name(Component.translatable("controlify.gui.radial_menu").withStyle(ChatFormatting.GOLD))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.radial_menu.tooltip"))
                        .text(newOptionLabel)
                        .build())
                .action((screen, opt) -> Minecraft.getInstance().setScreen(new RadialMenuScreen(
                        controller,
                        null,
                        RadialItems.createBindings(controller),
                        Component.empty(),
                        new RadialItems.BindingEditMode(controller),
                        screen
                )))
                .text(Component.translatable("controlify.gui.radial_menu.btn_text"))
                .build();
        Option<?> radialBind = createBindingOpt(ControlifyBindings.RADIAL_MENU, controller)
                .listener((opt, val) -> updateConflictingBinds(optionBinds))
                .build();
        optionBinds.add(new OptionBindPair(radialBind, ControlifyBindings.RADIAL_MENU.on(controller)));
        category.option(editRadialButton);
        category.option(radialBind);
        category.option(Option.<Integer>createBuilder()
                        .name(Component.translatable("controlify.gui.radial_menu.btn_focus_timeout"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.radial_menu.btn_focus_timeout.tooltip"))
                                .build())
                        .binding(def.radialButtonFocusTimeoutTicks,
                                () -> config.radialButtonFocusTimeoutTicks,
                                v -> config.radialButtonFocusTimeoutTicks = v)
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(2, 40).step(1).formatValue(ticksToMillisFormatter))
                        .build());

        groupBindings(input.getAllBindings()).forEach((categoryName, bindGroup) -> {
            var controlsGroup = OptionGroup.createBuilder()
                    .name(categoryName);

            controlsGroup.options(bindGroup.stream().flatMap(binding -> {
                if (binding != ControlifyBindings.RADIAL_MENU.on(controller)) {
                    Option.Builder<?> option = createBindingOpt(binding, controller)
                            .listener((opt, val) -> updateConflictingBinds(optionBinds));

                    Option<?> built = option.build();
                    optionBinds.add(new OptionBindPair(built, binding));
                    return Stream.of(built);
                }
                return Stream.empty();
            }).toList());

            category.group(controlsGroup.build());
        });
        updateConflictingBinds(optionBinds);

        category.option(ButtonOption.createBuilder()
                .name(Component.translatable("controlify.gui.reset_all_binds"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.reset_all_binds.tooltip"))
                        .build())
                .action((screen, opt) -> {
                    for (OptionBindPair pair : optionBinds) {
                        // who needs a type system?
                        ((Option<Object>) pair.option()).requestSet(pair.binding.defaultInput());
                    }
                })
                .build());

        return Optional.of(category.build());
    }

    private void updateConflictingBinds(List<OptionBindPair> all) {
        all.forEach(pair -> ((BindController) pair.option().controller()).setConflicting(false));

        for (OptionBindPair opt : all) {
            Set<BindContext> ctxs = opt.binding().contexts();

            List<OptionBindPair> conflicting = all.stream()
                    .filter(pair -> pair.binding() != opt.binding())
                    .filter(pair -> {
                        boolean contextsMatch = pair.binding().contexts()
                                .stream()
                                .anyMatch(ctxs::contains);
                        boolean bindMatches = pair.option().pendingValue().equals(opt.option().pendingValue());
                        boolean bindIsNotEmpty = !(pair.option().pendingValue() instanceof EmptyInput);
                        return contextsMatch && bindMatches && bindIsNotEmpty;
                    }).toList();

            conflicting.forEach(conflict -> ((BindController) conflict.option().controller()).setConflicting(true));
        }
    }

    private static Map<Component, List<InputBinding>> groupBindings(Collection<InputBinding> bindings) {
        return bindings.stream()
                .collect(Collectors.groupingBy(InputBinding::category, LinkedHashMap::new, Collectors.toList()));
    }

    private static Option.Builder<Input> createBindingOpt(InputBindingSupplier bindingSupplier, ControllerEntity controller) {
        return createBindingOpt(bindingSupplier.on(controller), controller);
    }

    private static Option.Builder<Input> createBindingOpt(InputBinding binding, ControllerEntity controller) {
        return Option.<Input>createBuilder()
                .name(binding.name())
                .description(v -> OptionDescription.createBuilder()
                        .text(binding.description())
                        .text(Component.translatable("controlify.gui.bind.currently_bound_to",
                                Component.empty()
                                        .append(Controlify.instance().inputFontMapper().getComponentFromInputs(
                                                controller.info().type().namespace(),
                                                v.getRelevantInputs()
                                        ))
                                        .append(CommonComponents.SPACE)
                                        .append(Inputs.getInputComponentAnd(v.getRelevantInputs()))
                        ))
                        .text(v.equals(binding.defaultInput()) ? Component.empty() : Component.translatable("controlify.gui.bind.default_bound_to",
                                Component.empty()
                                        .append(Controlify.instance().inputFontMapper().getComponentFromInputs(
                                                controller.info().type().namespace(),
                                                binding.defaultInput().getRelevantInputs()
                                        ))
                                        .append(CommonComponents.SPACE)
                                        .append(Inputs.getInputComponentAnd(
                                                binding.defaultInput().getRelevantInputs()
                                        ))
                        ))
                        .build())
                .binding(EmptyInput.INSTANCE, binding::boundInput, binding::setBoundInput)
                .customController(opt -> new BindController(opt, controller));
    }

    private static ResourceLocation screenshot(String filename) {
        return CUtil.rl("textures/screenshots/" + filename);
    }

    private record OptionBindPair(Option<?> option, InputBinding binding) {
    }
}
