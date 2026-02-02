package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.guide.GuideVerbosity;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindApiImpl;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.bindings.input.EmptyInput;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.config.settings.profile.GenericControllerSettings;
import dev.isxander.controlify.config.settings.profile.InputSettings;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.gyro.GyroButtonMode;
import dev.isxander.controlify.controller.gyro.GyroYawMode;
import dev.isxander.controlify.controller.input.DeadzoneGroup;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.input.Inputs;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.gui.controllers.BindController;
import dev.isxander.controlify.gui.controllers.Deadzone2DImageRenderer;
import dev.isxander.controlify.ingame.InputCurves;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.server.ServerPolicies;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ControllerConfigScreenFactory {
    private static final ValueFormatter<Float> percentFormatter = v -> Component.literal(String.format("%.0f%%", v*100));
    private static final ValueFormatter<Float> percentOrOffFormatter = v -> v == 0 ? CommonComponents.OPTION_OFF : percentFormatter.format(v);
    private static final Component newOptionLabel = Component.translatable("controlify.gui.new_options.label").withStyle(ChatFormatting.GOLD);
    private static final ValueFormatter<Integer> ticksToMillisFormatter = v -> Component.literal(String.format("%03dms", v * 50));

    private final List<Option<?>> newOptions = new ArrayList<>();

    public static Screen generateConfigScreen(
            Screen parent,
            ProfileSettings settings, ProfileSettings defaults,
            @Nullable ControllerEntity controller
    ) {
        return new ControllerConfigScreenFactory().generateConfigScreen0(
                parent,
                settings,
                defaults,
                Optional.ofNullable(controller)
        );
    }

    private Screen generateConfigScreen0(
            Screen parent,
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        var advancedCategory = createAdvancedCategory(settings, defaults, controller);
        var bindsCategory = makeBindsCategory(settings, defaults, controller);
        var basicCategory = createBasicCategory(settings, defaults, controller); // must be last for new options

        var yacl = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Controlify"))
                .category(basicCategory)
                .category(advancedCategory)
                .save(() -> Controlify.instance().config().saveSafely());

        bindsCategory.ifPresent(yacl::category);

        return yacl.build().generateScreen(parent);
    }

    private ConfigCategory createBasicCategory(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        var sensitivityGroup = makeSensitivityGroup(settings, defaults, controller);
        var controlsGroup = makeControlsGroup(settings, defaults, controller);
        var accessibilityGroup = makeAccessibilityGroup(settings, defaults, controller);
        var deadzoneGroup = makeDeadzoneGroup(settings, defaults, controller);

        GenericControllerSettings genSettings = settings.generic;
        GenericControllerSettings genDefaults = defaults.generic;

        ConfigCategory.Builder builder = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.config.category.basic"));
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

    private Optional<OptionGroup> makeSensitivityGroup(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        var builder = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.config.group.sensitivity"));

        if (controller.isPresent() && controller.get().input().isEmpty()) {
            builder.option(LabelOption.create(Component.literal("TODO warning no input component")));
        }

        InputSettings.SensitivitySettings sens = settings.input.sensitivity;
        InputSettings.SensitivitySettings def = defaults.input.sensitivity;

        return Optional.of(builder
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.horizontal_look_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.horizontal_look_sensitivity.tooltip"))
                                .build())
                        .binding(def.hLookSensitivity, () -> sens.hLookSensitivity, v -> sens.hLookSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).formatValue(percentFormatter))
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.vertical_look_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.vertical_look_sensitivity.tooltip"))
                                .build())
                        .binding(def.vLookSensitivity, () -> sens.vLookSensitivity, v -> sens.vLookSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).formatValue(percentFormatter))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.invert_vertical_look"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.invert_vertical_look.tooltip"))
                                .build())
                        .binding(def.vLookInvert, () -> sens.vLookInvert, v -> sens.vLookInvert = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Float>createBuilder()
                        .name(Component.translatable("controlify.gui.vmouse_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.vmouse_sensitivity.tooltip"))
                                .build())
                        .binding(def.virtualMouseSensitivity, () -> sens.virtualMouseSensitivity, v -> sens.virtualMouseSensitivity = v)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.1f, 2f).step(0.05f).formatValue(percentFormatter))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.reduce_aiming_sensitivity"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.reduce_aiming_sensitivity.tooltip"))
                                .webpImage(screenshot("reduce-aim-sensitivity.webp"))
                                .build())
                        .binding(def.reduceAimingSensitivity, () -> sens.reduceAimingSensitivity, v -> sens.reduceAimingSensitivity = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<InputCurves>createBuilder()
                        .name(Component.translatable("controlify.gui.look_input_curve"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.look_input_curve.tooltip"))
                                .build())
                        .binding(def.lookInputCurve, () -> sens.lookInputCurve, v -> sens.lookInputCurve = v)
                        .controller(opt -> EnumControllerBuilder.create(opt)
                                .enumClass(InputCurves.class))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.is_lce"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.is_lce.tooltip"))
                                .build())
                        .binding(def.isLCE, () -> sens.isLCE, v -> sens.isLCE = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .onOffFormatter())
                        .build())
                .build());
    }

    private Optional<OptionGroup> makeControlsGroup(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        if (controller.isEmpty())
            return Optional.empty();

        ValueFormatter<Boolean> holdToggleFormatter = v -> Component.translatable("controlify.gui.format.hold_toggle." + (v ? "toggle" : "hold"));

        GenericControllerSettings config = settings.generic;
        GenericControllerSettings def = defaults.generic;

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
                                .text(!ServerPolicies.DISABLE_FLY_DRIFTING.isUnset() ? Component.translatable("controlify.gui.server_controlled").withStyle(ChatFormatting.GOLD) : Component.empty())
                                .build())
                        .binding(
                                def.disableFlyDrifting,
                                () -> ServerPolicies.DISABLE_FLY_DRIFTING.isUnset()
                                        ? config.disableFlyDrifting
                                        : ServerPolicies.DISABLE_FLY_DRIFTING.get(),
                                v -> config.disableFlyDrifting = v
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .available(ServerPolicies.DISABLE_FLY_DRIFTING.isUnset())
                        .build())
                .build());
    }

    private Optional<OptionGroup> makeAccessibilityGroup(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        GenericControllerSettings gSettings = settings.generic;
        GenericControllerSettings gDefaults = defaults.generic;

        return Optional.of(OptionGroup.createBuilder()
                .name(Component.translatable("controlify.config.group.accessibility"))
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.show_ingame_guide"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.show_ingame_guide.tooltip"))
                                .image(screenshot("ingame-button-guide.png"), 961, 306)
                                .build())
                        .binding(gDefaults.guide.showIngameGuide, () -> gSettings.guide.showIngameGuide, v -> gSettings.guide.showIngameGuide = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.ingame_button_guide_position"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.ingame_button_guide_position.tooltip")))
                        .binding(gDefaults.guide.ingameGuideBottom, () -> gSettings.guide.ingameGuideBottom, v -> gSettings.guide.ingameGuideBottom = v)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .formatValue(v -> Component.translatable(v ? "controlify.gui.format.bottom" : "controlify.gui.format.top")))
                        .build())
                .option(Option.<GuideVerbosity>createBuilder()
                        .name(Component.translatable("controlify.gui.guide_verbosity"))
                        .description(OptionDescription.of(Component.translatable("controlify.gui.guide_verbosity.tooltip")))
                        .binding(gDefaults.guide.verbosity, () -> gSettings.guide.verbosity, v -> gSettings.guide.verbosity = v)
                        .controller(opt -> CyclingListControllerBuilder.create(opt)
                                .values(GuideVerbosity.values())
                                .formatValue(NameableEnum::getDisplayName))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.show_screen_guide"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.show_screen_guide.tooltip"))
                                .webpImage(screenshot("screen-button-guide.webp"))
                                .build())
                        .binding(gDefaults.guide.showScreenGuides, () -> gSettings.guide.showScreenGuides, v -> gSettings.guide.showScreenGuides = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("controlify.gui.show_keyboard"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.show_keyboard.tooltip"))
                                .build())
                        .binding(gDefaults.keyboard.showOnScreenKeyboard, () -> gSettings.keyboard.showOnScreenKeyboard, v -> gSettings.keyboard.showOnScreenKeyboard = v)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build());
    }

    private Optional<OptionGroup> makeDeadzoneGroup(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        Optional<InputComponent> inputOpt = controller.flatMap(ControllerEntity::input);
        if (inputOpt.isEmpty())
            return Optional.empty();

        InputComponent input = inputOpt.get();
        InputSettings config = settings.input;
        InputSettings def = defaults.input;

        var deadzoneOpts = new ArrayList<Option<Float>>();

        var group = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.config.group.deadzones"));

        group.option(LabelOption.create(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED)));

        for (DeadzoneGroup deadzoneGroup : input.getDeadzoneGroups().values()) {
            Identifier groupName = deadzoneGroup.name();
            Component name = Component.translatable("controlify.deadzone_group." + groupName.getNamespace() + "." + groupName.getPath());

            AtomicReference<Option<Float>> deadzoneRef = new AtomicReference<>();
            Option<Float> deadzoneOpt = Option.<Float>createBuilder()
                    .name(name)
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.axis_deadzone.tooltip", name))
                            .customImage(CompletableFuture.completedFuture(deadzoneGroup.axes().size() == 4 ? Optional.of(new Deadzone2DImageRenderer(input, deadzoneGroup, deadzoneRef::get)) : Optional.empty()))
                            .build())
                    .binding(
                            def.sensitivity.getDeadzone(groupName),
                            () -> config.sensitivity.getDeadzone(groupName),
                            v -> config.sensitivity.putDeadzone(groupName, v)
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

        return Optional.of(group.build());
    }

    private ConfigCategory createAdvancedCategory(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        var builder = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.config.category.advanced"));

        makeVibrationGroup(settings, defaults, controller).ifPresent(builder::group);
        makeGyroGroup(settings, defaults, controller).ifPresent(builder::group);
        makeControllerMappingGroup(settings, defaults, controller).ifPresent(builder::group);

        return builder.build();
    }

    private Optional<OptionGroup> makeControllerMappingGroup(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        Optional<InputComponent> inputOpt = controller.flatMap(ControllerEntity::input);
        if (inputOpt.isEmpty())
            return Optional.empty();
        InputComponent input = inputOpt.get();

        DeviceSettings config = Controlify.instance().config().getSettings()
                .getOrCreateDeviceSettings(controller.get().uid());
        DeviceSettings def = DeviceSettings.defaults();

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

    private Optional<OptionGroup> makeVibrationGroup(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        var vibrationGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.vibration"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.group.vibration.tooltip"))
                        .build());

        if (controller.isPresent() && controller.get().rumble().isEmpty()) {
            vibrationGroup.option(LabelOption.create(notSupportedText(Component.translatable("controlify.gui.group.vibration"))));
        }

        List<Option<Float>> strengthOptions = new ArrayList<>();
        Option<Boolean> allowVibrationOption;
        vibrationGroup.option(allowVibrationOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("controlify.gui.allow_vibrations"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.allow_vibrations.tooltip"))
                        .build())
                .binding(defaults.rumble.enabled, () -> settings.rumble.enabled, v -> settings.rumble.enabled = v)
                .addListener((opt, event) -> strengthOptions.forEach(so -> so.setAvailable(opt.pendingValue())))
                .controller(TickBoxControllerBuilder::create)
                .build());

        boolean hdHapticsNotSupported = controller.isPresent() && controller.get().hdHaptics().isEmpty();
        vibrationGroup.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("controlify.gui.hd_haptics"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.hd_haptics.tooltip"))
                        .text(hdHapticsNotSupported ? notSupportedText(Component.translatable("controlify.gui.hd_haptics")) : Component.empty())
                        .build())
                .binding(defaults.hdHaptic.enabled, () -> settings.hdHaptic.enabled, v -> settings.hdHaptic.enabled = v)
                .controller(TickBoxControllerBuilder::create)
                .build());

        for (RumbleSource source : RumbleSource.values()) {
            var option = Option.<Float>createBuilder()
                    .name(Component.translatable("controlify.vibration_strength." + source.id().getNamespace() + "." + source.id().getPath()))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.vibration_strength." + source.id().getNamespace() + "." + source.id().getPath() + ".tooltip"))
                            .build())
                    .binding(
                            defaults.rumble.vibrationStrengths.getOrDefault(source.id(), 1f),
                            () -> settings.rumble.vibrationStrengths.getOrDefault(source.id(), 1f),
                            v -> settings.rumble.vibrationStrengths.put(source.id(), v)
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
        if (controller.isPresent() && controller.get().rumble().isPresent()) {
            RumbleComponent rumble = controller.get().rumble().get();
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
        }

        return Optional.of(vibrationGroup.build());
    }

    private Optional<OptionGroup> makeGyroGroup(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {
        var gyroGroup = OptionGroup.createBuilder()
                .name(Component.translatable("controlify.gui.group.gyro"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.group.gyro.tooltip"))
                        .build());

        if (controller.isPresent() && controller.get().gyro().isEmpty()) {
            gyroGroup.collapsed(true);
            gyroGroup.option(LabelOption.create(Component.translatable("controlify.gui.group.gyro.no_gyro.tooltip").withStyle(ChatFormatting.RED)));
        }

        Option<Float> gyroSensitivity;
        List<Option<?>> gyroOptions = new ArrayList<>();
        gyroGroup.option(gyroSensitivity = Option.<Float>createBuilder()
                .name(Component.translatable("controlify.gui.gyro_look_sensitivity"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.gyro_look_sensitivity.tooltip"))
                        .build())
                .binding(defaults.gyro.lookSensitivity, () -> settings.gyro.lookSensitivity, v -> settings.gyro.lookSensitivity = v)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(0f, 3f)
                        .step(0.1f)
                        .formatValue(percentOrOffFormatter))
                .addListener((opt, event) -> gyroOptions.forEach(o -> {
                    o.setAvailable(opt.pendingValue() > 0);
                    o.requestSetDefault();
                }))
                .build());
        var relativeModeOpt = Option.<Boolean>createBuilder()
                .name(Component.translatable("controlify.gui.gyro_behaviour"))
                .description(val -> OptionDescription.createBuilder()
                        .text(Component.translatable("controlify.gui.gyro_behaviour.tooltip"))
                        .text(val ? Component.translatable("controlify.gui.gyro_behaviour.relative.tooltip") : Component.translatable("controlify.gui.gyro_behaviour.absolute.tooltip"))
                        .build())
                .binding(defaults.gyro.relativeMode, () -> settings.gyro.relativeMode, v -> settings.gyro.relativeMode = v)
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
                    .binding(defaults.gyro.yawMode, () -> settings.gyro.yawMode, v -> settings.gyro.yawMode = v)
                    .controller(opt -> EnumControllerBuilder.create(opt).enumClass(GyroYawMode.class))
                    .build();
            gyroOptions.add(option);
            return option;
        }));
        gyroGroup.option(Util.make(() -> {
            var opt = Option.<Boolean>createBuilder()
                    .name(Component.translatable("controlify.gui.gyro_invert_x"))
                    .description(OptionDescription.of(Component.translatable("controlify.gui.gyro_invert_x.tooltip")))
                    .binding(defaults.gyro.invertYaw, () -> settings.gyro.invertYaw, v -> settings.gyro.invertYaw = v)
                    .controller(TickBoxControllerBuilder::create)
                    .build();
            gyroOptions.add(opt);
            return opt;
        }));
        gyroGroup.option(Util.make(() -> {
            var opt = Option.<Boolean>createBuilder()
                    .name(Component.translatable("controlify.gui.gyro_invert_y"))
                    .description(OptionDescription.of(Component.translatable("controlify.gui.gyro_invert_y.tooltip")))
                    .binding(defaults.gyro.invertPitch, () -> settings.gyro.invertPitch, v -> settings.gyro.invertPitch = v)
                    .controller(TickBoxControllerBuilder::create)
                    .build();
            gyroOptions.add(opt);
            return opt;
        }));
        gyroGroup.option(Util.make(() -> {
            var opt = Option.<GyroButtonMode>createBuilder()
                    .name(Component.translatable("controlify.gui.gyro_requires_button"))
                    .description(val -> OptionDescription.createBuilder()
                            .text(Component.translatable("controlify.gui.gyro_requires_button.tooltip"))
                            .text(val == GyroButtonMode.ON ? Component.translatable("controlify.gui.gyro_requires_button.tooltip.on") : Component.empty())
                            .text(val == GyroButtonMode.INVERT ? Component.translatable("controlify.gui.gyro_requires_button.tooltip.invert") : Component.empty())
                            .text(val == GyroButtonMode.TOGGLE ? Component.translatable("controlify.gui.gyro_requires_button.tooltip.toggle") : Component.empty())
                            .text(val == GyroButtonMode.OFF ? Component.translatable("controlify.gui.gyro_requires_button.tooltip.off") : Component.empty())
                            .build())
                    .binding(defaults.gyro.buttonMode, () -> settings.gyro.buttonMode, v -> settings.gyro.buttonMode = v)
                    .controller(controllerOpt -> EnumControllerBuilder.create(controllerOpt).enumClass(GyroButtonMode.class))
                    .available(gyroSensitivity.pendingValue() > 0)
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
                    .binding(defaults.gyro.flickStick, () -> settings.gyro.flickStick, v -> settings.gyro.flickStick = v)
                    .controller(TickBoxControllerBuilder::create)
                    .available(gyroSensitivity.pendingValue() > 0)
                    .build();
            gyroOptions.add(opt);
            return opt;
        }));

        return Optional.of(gyroGroup.build());
    }

    private Optional<ConfigCategory> makeBindsCategory(
            ProfileSettings settings,
            ProfileSettings defaults,
            Optional<ControllerEntity> controller
    ) {

        var category = ConfigCategory.createBuilder()
                .name(Component.translatable("controlify.gui.group.controls"));

        InputSettings.RadialMenuSettings radialConfig = settings.input.radialMenu;
        InputSettings.RadialMenuSettings radialDef = defaults.input.radialMenu;

        List<OptionBindPair> optionBinds = new ArrayList<>();

        category
                .optionIf(controller.isPresent(), ButtonOption.createBuilder()
                        .name(Component.translatable("controlify.gui.radial_menu").withStyle(ChatFormatting.GOLD))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.radial_menu.tooltip"))
                                .build())
                        .action((screen, opt) -> Minecraft.getInstance().setScreen(new RadialMenuScreen(
                                controller.orElseThrow(),
                                null,
                                RadialItems.createBindings(controller.get()),
                                Component.empty(),
                                new RadialItems.BindingEditMode(controller.get()),
                                screen
                        )))
                        .text(Component.translatable("controlify.gui.radial_menu.btn_text"))
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(Component.translatable("controlify.gui.radial_menu.btn_focus_timeout"))
                        .description(OptionDescription.createBuilder()
                                .text(Component.translatable("controlify.gui.radial_menu.btn_focus_timeout.tooltip"))
                                .build())
                        .binding(radialDef.radialButtonFocusTimeoutTicks,
                                () -> radialConfig.radialButtonFocusTimeoutTicks,
                                v -> radialConfig.radialButtonFocusTimeoutTicks = v)
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(2, 40).step(1).formatValue(ticksToMillisFormatter))
                        .build());

        Collection<InputBinding> allBindings = controller.isPresent()
                ? controller.get().input().map(InputComponent::getAllBindings).orElse(List.of())
                : ControlifyBindApiImpl.INSTANCE.provideBindsForController(null); // when giving null as controller, we get all binds

        groupBindings(allBindings).forEach((categoryName, bindGroup) -> {
            var controlsGroup = OptionGroup.createBuilder()
                    .name(categoryName);

            controlsGroup.options(bindGroup.stream().map(binding -> {
                Option.Builder<?> option = createBindingOpt(binding, controller.orElse(null))
                        .addListener((opt, val) -> updateConflictingBinds(optionBinds));

                Option<?> built = option.build();
                optionBinds.add(new OptionBindPair(built, binding));
                return built;
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
                        // noinspection unchecked
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

    private static Option.Builder<Input> createBindingOpt(InputBinding binding, @Nullable ControllerEntity controller) {
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

    private static Identifier screenshot(String filename) {
        return CUtil.rl("textures/screenshots/" + filename);
    }

    private static MutableComponent notSupportedText(Component featureName) {
        return Component.translatable("controlify.gui.not_supported", featureName).withStyle(ChatFormatting.RED);
    }

    private record OptionBindPair(Option<?> option, InputBinding binding) {
    }
}
