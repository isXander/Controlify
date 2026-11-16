/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.screen;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.contextual.GuideVerbosity;
import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ActionDispatcher;
import dev.isxander.controlify.impl.action.activator.ActivatorConfig;
import dev.isxander.controlify.impl.action.ActionLayerRegistryImpl;
import dev.isxander.controlify.impl.action.BindingConflicts;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.impl.action.ActionImpl;
import dev.isxander.controlify.impl.action.ActionSpecRegistryImpl;
import dev.isxander.controlify.impl.config.settings.device.DeviceSettings;
import dev.isxander.controlify.impl.config.settings.profile.FlickStickSettings;
import dev.isxander.controlify.impl.config.settings.profile.GenericControllerSettings;
import dev.isxander.controlify.impl.config.settings.profile.GyroSettings;
import dev.isxander.controlify.impl.config.settings.profile.InputSettings;
import dev.isxander.controlify.impl.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.gyro.GyroActivation;
import dev.isxander.controlify.impl.controller.gyro.GyroSpace;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import dev.isxander.controlify.impl.feedback.FeedbackSource;
import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import dev.isxander.controlify.impl.feedback.FeedbackRegistryImpl;
import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.api.gui.font.ActionGlyphs;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.gui.controllers.BindController;
import java.util.Locale;
import net.minecraft.network.chat.ComponentUtils;
import dev.isxander.controlify.impl.gui.controllers.BindingInfoImageRenderer;
import dev.isxander.controlify.impl.gui.controllers.ControllerSpriteImageRenderer;
import dev.isxander.controlify.impl.gui.controllers.Deadzone2DImageRenderer;
import dev.isxander.controlify.impl.ingame.look.FlickSnap;
import dev.isxander.controlify.impl.ingame.InputCurves;
import dev.isxander.controlify.impl.input.mapping.DeadzoneGroup;
import dev.isxander.controlify.impl.server.ServerPolicies;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ControllerConfigScreenFactory {
	private static final ValueFormatter<Float> percentFormatter = v -> Component.literal(String.format("%.0f%%", v*100));
	private static final ValueFormatter<Float> percentOrOffFormatter = v -> v == 0 ? CommonComponents.OPTION_OFF : percentFormatter.format(v);
	private static final Component newOptionLabel = Component.translatable("controlify.gui.new_options.label").withStyle(ChatFormatting.GOLD);
	private static final ValueFormatter<Integer> ticksToMillisFormatter = v -> Component.literal(String.format("%03dms", v * 50));
	private static final ValueFormatter<Integer> millisFormatter = v -> Component.literal(v + "ms");

	private final List<Option<?>> newOptions = new ArrayList<>();

	public static Screen generateConfigScreen(
			Screen parent,
			ProfileSettings settings, ProfileSettings defaults,
			@Nullable ControllerImpl controller
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
			Optional<ControllerImpl> controller
	) {
		String initialControllerUid = settings.controllerUid;
		var aimingCategory = createAimingCategory(settings, defaults, controller);
		var bindsCategory = makeBindsCategory(settings, defaults, controller);
		var interfaceCategory = createInterfaceCategory(settings, defaults, controller);
		var hapticsCategory = createHapticsCategory(settings, defaults, controller);
		var hardwareCategory = createHardwareCategory(settings, defaults, controller);
		var generalCategory = createGeneralCategory(settings, defaults, controller); // must be last for new options

		var yacl = YetAnotherConfigLib.createBuilder()
				.title(Component.literal("Controlify"))
				.category(generalCategory)
				.category(aimingCategory)
				.save(() -> {
					if (!Objects.equals(initialControllerUid, settings.controllerUid)) {
						Controlify.instance().applyControllerSelection(true);
					}
					Controlify.instance().config().saveSafely();
				});

		bindsCategory.ifPresent(yacl::category);
		yacl.category(interfaceCategory);
		yacl.category(hapticsCategory);
		hardwareCategory.ifPresent(yacl::category);

		return yacl.build().generateScreen(parent);
	}

	private ConfigCategory createGeneralCategory(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		var gameplayGroup = makeGameplayGroup(settings, defaults, controller);

		ConfigCategory.Builder builder = ConfigCategory.createBuilder()
				.name(Component.translatable("controlify.gui.config.category.general"));
		builder.group(makeProfileGroup(settings));
		if (!newOptions.isEmpty()) {
			builder.group(OptionGroup.createBuilder()
					.name(Component.translatable("controlify.gui.new_options").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
					.description(OptionDescription.of(Component.translatable("controlify.gui.new_options.tooltip")))
					.options(newOptions)
					.build());
		}

		gameplayGroup.ifPresent(builder::group);

		return builder.build();
	}

	private ConfigCategory createAimingCategory(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		var builder = ConfigCategory.createBuilder()
				.name(Component.translatable("controlify.gui.config.category.aiming"));

		makeStickGroup(settings, defaults, controller).ifPresent(builder::group);
		makeFlickStickGroup(settings, defaults, controller).ifPresent(builder::group);
		makeGyroGroups(settings, defaults, controller).forEach(builder::group);

		return builder.build();
	}

	private ConfigCategory createInterfaceCategory(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		var builder = ConfigCategory.createBuilder()
				.name(Component.translatable("controlify.gui.config.category.interface"));

		makeButtonGuidesGroup(settings, defaults, controller).ifPresent(builder::group);
		makeOnScreenKeyboardGroup(settings, defaults, controller).ifPresent(builder::group);
		makeVirtualMouseGroup(settings, defaults, controller).ifPresent(builder::group);

		return builder.build();
	}

	private ConfigCategory createHapticsCategory(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		var builder = ConfigCategory.createBuilder()
				.name(Component.translatable("controlify.gui.config.category.haptics"));

		makeVibrationGroup(settings, defaults, controller).ifPresent(builder::group);
		makeTriggerEffectsGroup(settings, defaults, controller).ifPresent(builder::group);

		return builder.build();
	}

	private Optional<ConfigCategory> createHardwareCategory(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		List<OptionGroup> groups = new ArrayList<>();
		makeDeadzoneGroup(settings, defaults, controller).ifPresent(groups::add);
		makeActivationGroup(settings, defaults, controller).ifPresent(groups::add);
		makeControllerMappingGroup(settings, defaults, controller).ifPresent(groups::add);
		if (groups.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(ConfigCategory.createBuilder()
				.name(Component.translatable("controlify.gui.config.category.hardware"))
				.groups(groups)
				.build());
	}

	private OptionGroup makeProfileGroup(ProfileSettings settings) {
		Map<String, DeviceSettings> devices = Controlify.instance().config().getSettings().deviceSettings();
		List<String> controllerChoices = devices.entrySet().stream()
				.sorted(Map.Entry.<String, DeviceSettings>comparingByValue(
						Comparator.comparingLong(device -> device.lastSeen)
				).reversed())
				.map(Map.Entry::getKey)
				.collect(Collectors.toCollection(ArrayList::new));
		if (settings.controllerUid != null && !controllerChoices.contains(settings.controllerUid)) {
			controllerChoices.add(settings.controllerUid);
		}
		controllerChoices.addFirst("");

		return OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.config.group.profile"))
				.option(Option.<String>createBuilder()
						.name(Component.translatable("controlify.gui.profile_name"))
						.description(OptionDescription.of(Component.translatable("controlify.gui.profile_name.tooltip")))
						.binding("", () -> Optional.ofNullable(settings.name).orElse(""), value ->
								settings.name = value.isBlank() ? null : value)
						.controller(StringControllerBuilder::create)
						.build())
				.option(Option.<String>createBuilder()
						.name(Component.translatable("controlify.gui.controller_selection"))
						.description(uid -> controllerSelectionDescription(uid, devices.get(uid)))
						.binding("", () -> Optional.ofNullable(settings.controllerUid).orElse(""), value ->
								settings.controllerUid = value.isEmpty() ? null : value)
						.controller(option -> CyclingListControllerBuilder.create(option)
								.values(controllerChoices)
								.formatValue(uid -> {
									if (uid.isEmpty()) {
										return Component.translatable("controlify.gui.controller_selection.automatic");
									}
									DeviceSettings device = devices.get(uid);
									return Component.literal(device == null || device.name.isBlank() ? uid : device.name);
								}))
						.build())
				.build();
	}

	private static OptionDescription controllerSelectionDescription(String uid, @Nullable DeviceSettings device) {
		OptionDescription.Builder builder = OptionDescription.createBuilder()
				.text(Component.translatable("controlify.gui.controller_selection.tooltip"));
		if (uid.isEmpty()) {
			return builder.build();
		}

		ControllerImpl connected = Controlify.instance().getControllerManager()
				.flatMap(manager -> manager.getControllerByUid(uid))
				.orElse(null);

		ControllerType type = connected != null ? connected.info().type() : null;
		Identifier typeId = type != null
				? type.id()
				: device != null ? device.controllerType : ControllerType.DEFAULT.id();
		Identifier sprite = type != null ? type.getIconSprite() : typeId.withPrefix("controllers/");

		builder.customImage(new ControllerSpriteImageRenderer(sprite));
		builder.text(Component.translatable("controlify.gui.controller_selection.info.uid", uid).withStyle(ChatFormatting.GRAY));
		builder.text(Component.translatable("controlify.gui.controller_selection.info.type", typeId.toString()).withStyle(ChatFormatting.GRAY));
		if (connected != null) {
			builder.text(Component.translatable("controlify.gui.controller_selection.info.driver",
					connected.driver().getClass().getSimpleName()).withStyle(ChatFormatting.GRAY));
		} else {
			builder.text(Component.translatable("controlify.gui.controller_selection.info.not_connected").withStyle(ChatFormatting.GRAY));
		}
		return builder.build();
	}

	private Optional<OptionGroup> makeStickGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		var builder = OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.config.group.stick"));

		InputSettings.SensitivitySettings sens = settings.input.sensitivity;
		InputSettings.SensitivitySettings def = defaults.input.sensitivity;

		Option<InputCurves> lookInputCurveOpt;

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
						.name(Component.translatable("controlify.gui.invert_horizontal_look"))
						.description(OptionDescription.createBuilder()
								.text(Component.translatable("controlify.gui.invert_horizontal_look.tooltip"))
								.build())
						.binding(def.hLookInvert, () -> sens.hLookInvert, v -> sens.hLookInvert = v)
						.controller(TickBoxControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("controlify.gui.invert_vertical_look"))
						.description(OptionDescription.createBuilder()
								.text(Component.translatable("controlify.gui.invert_vertical_look.tooltip"))
								.build())
						.binding(def.vLookInvert, () -> sens.vLookInvert, v -> sens.vLookInvert = v)
						.controller(TickBoxControllerBuilder::create)
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
				.option(lookInputCurveOpt = Option.<InputCurves>createBuilder()
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
						.addListener((opt, event) -> {
							lookInputCurveOpt.setAvailable(!opt.pendingValue());
						})
						.build())
				.build());
	}

	private Optional<OptionGroup> makeGameplayGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		if (controller.isEmpty()) {
			return Optional.empty();
		}

		ValueFormatter<Boolean> holdToggleFormatter = v -> Component.translatable("controlify.gui.format.hold_toggle." + (v ? "toggle" : "hold"));

		GenericControllerSettings config = settings.generic;
		GenericControllerSettings def = defaults.generic;

		return Optional.of(OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.config.group.gameplay"))
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

	private Optional<OptionGroup> makeButtonGuidesGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		GenericControllerSettings gSettings = settings.generic;
		GenericControllerSettings gDefaults = defaults.generic;

		return Optional.of(OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.config.group.button_guides"))
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
				.option(Option.<Integer>createBuilder()
						.name(Component.translatable("controlify.gui.ingame_guide_gui_scale"))
						.description(OptionDescription.createBuilder()
								.text(Component.translatable("controlify.gui.ingame_guide_gui_scale.tooltip"))
						.build())
						.binding(gDefaults.guide.ingameGuiScale, () -> gSettings.guide.ingameGuiScale, v -> gSettings.guide.ingameGuiScale = v)
						.controller(opt -> IntegerSliderControllerBuilder.create(opt)
								.range(-1, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()))
								.step(1)
								.formatValue(v -> switch (v) {
									case -1 -> Component.translatable("controlify.gui.guide_gui_scale.unchanged");
									case 0 -> Component.translatable("controlify.gui.guide_gui_scale.auto");
									default -> Component.literal(String.valueOf(v));
								}))
						.build())
				.option(Option.<Integer>createBuilder()
						.name(Component.translatable("controlify.gui.screen_guide_gui_scale"))
						.description(OptionDescription.createBuilder()
								.text(Component.translatable("controlify.gui.screen_guide_gui_scale.tooltip"))
								.build())
						.binding(gDefaults.guide.screenGuiScale, () -> gSettings.guide.screenGuiScale, v -> gSettings.guide.screenGuiScale = v)
						.controller(opt -> IntegerSliderControllerBuilder.create(opt)
								.range(-1, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()))
								.step(1)
								.formatValue(v -> switch (v) {
									case -1 -> Component.translatable("controlify.gui.guide_gui_scale.unchanged");
									case 0 -> Component.translatable("controlify.gui.guide_gui_scale.auto");
									default -> Component.literal(String.valueOf(v));
								}))
						.build())
				.build());
	}

	private Optional<OptionGroup> makeOnScreenKeyboardGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		GenericControllerSettings gSettings = settings.generic;
		GenericControllerSettings gDefaults = defaults.generic;

		return Optional.of(OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.config.group.on_screen_keyboard"))
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

	private Optional<OptionGroup> makeVirtualMouseGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		InputSettings.SensitivitySettings sens = settings.input.sensitivity;
		InputSettings.SensitivitySettings def = defaults.input.sensitivity;

		return Optional.of(OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.config.group.virtual_mouse"))
				.option(Option.<Float>createBuilder()
						.name(Component.translatable("controlify.gui.vmouse_sensitivity"))
						.description(OptionDescription.createBuilder()
								.text(Component.translatable("controlify.gui.vmouse_sensitivity.tooltip"))
								.build())
						.binding(def.virtualMouseSensitivity, () -> sens.virtualMouseSensitivity, v -> sens.virtualMouseSensitivity = v)
						.controller(opt -> FloatSliderControllerBuilder.create(opt)
								.range(0.1f, 2f).step(0.05f).formatValue(percentFormatter))
						.build())
				.build());
	}

	private Optional<OptionGroup> makeDeadzoneGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		if (controller.isEmpty()) {
			return Optional.empty();
		}

		InputComponentImpl input = controller.get().input();
		InputSettings config = settings.input;
		InputSettings def = defaults.input;

		var deadzoneOpts = new ArrayList<Option<Float>>();

		var group = OptionGroup.createBuilder()
				.name(Component.translatable("controlify.config.group.deadzones"));

		group.option(LabelOption.create(Component.translatable("controlify.gui.stickdrift_warning").withStyle(ChatFormatting.RED)));

		for (DeadzoneGroup deadzoneGroup : input.controllerMapping().deadzones().values()) {
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

		return Optional.of(group.build());
	}

	private Optional<OptionGroup> makeActivationGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		if (controller.isEmpty()) {
			return Optional.empty();
		}

		InputSettings config = settings.input;
		InputSettings def = defaults.input;

		var group = OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.config.group.activation"));

		group.option(Option.<Float>createBuilder()
				.name(Component.translatable("controlify.gui.button_activation_threshold"))
				.description(OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.button_activation_threshold.tooltip"))
						.build())
				.binding(def.buttonActivationThreshold, () -> config.buttonActivationThreshold, v -> config.buttonActivationThreshold = v)
				.controller(opt -> FloatSliderControllerBuilder.create(opt)
						.range(0.01f, 1f).step(0.01f)
						.formatValue(percentFormatter))
				.build());

		InputSettings.GestureSettings gestures = config.gestures;
		InputSettings.GestureSettings gestureDefaults = def.gestures;
		group.option(Option.<Integer>createBuilder()
				.name(Component.translatable("controlify.gui.gesture_tap_max"))
				.description(OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.gesture_tap_max.tooltip"))
						.build())
				.binding(gestureDefaults.tapMaxMs, () -> gestures.tapMaxMs, v -> gestures.tapMaxMs = v)
				.controller(opt -> IntegerSliderControllerBuilder.create(opt)
						.range(50, 500).step(10)
						.formatValue(millisFormatter))
				.build());
		group.option(Option.<Integer>createBuilder()
				.name(Component.translatable("controlify.gui.gesture_long_press"))
				.description(OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.gesture_long_press.tooltip"))
						.build())
				.binding(gestureDefaults.longPressMs, () -> gestures.longPressMs, v -> gestures.longPressMs = v)
				.controller(opt -> IntegerSliderControllerBuilder.create(opt)
						.range(200, 1500).step(50)
						.formatValue(millisFormatter))
				.build());

		return Optional.of(group.build());
	}

	private Optional<OptionGroup> makeControllerMappingGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controllerOpt
	) {
		if (controllerOpt.isEmpty()) {
			return Optional.empty();
		}
		ControllerImpl controller = controllerOpt.get();

		DeviceSettings config = Controlify.instance().config().getSettings()
				.getOrCreateDeviceSettings(controller.uid());
		DeviceSettings def = DeviceSettings.defaults(controller.uid());

		return Optional.of(OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.group.controller_mapping"))
				.option(LabelOption.create(Component.translatable("controlify.gui.controller_mapping.explanation")))
				.option(ButtonOption.createBuilder()
						.name(Component.translatable("controlify.gui.create_gamepad_mapping"))
						.description(OptionDescription.of(Component.translatable("controlify.gui.create_gamepad_mapping.tooltip")))
						.action((screen, button) -> MinecraftUtil.setScreen(ControllerMappingMakerScreen.createGamepadMapping(controller, screen)))
						.build())
				.option(ButtonOption.createBuilder()
						.name(Component.translatable("controlify.gui.clear_mapping"))
						.description(OptionDescription.of(Component.translatable("controlify.gui.clear_mapping.tooltip")))
						.action((screen, button) -> {
							config.mapping = def.mapping;
							controller.input().reloadControllerMapping();
						})
						.build())
				.collapsed(true)
				.build());
	}

	private Optional<OptionGroup> makeVibrationGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
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

		boolean hdHapticsNotSupported = controller.isPresent()
				&& controller.get().pcmHaptics().isEmpty()
				&& controller.get().steamHaptics().isEmpty();
		vibrationGroup.option(Option.<Boolean>createBuilder()
				.name(Component.translatable("controlify.gui.hd_haptics"))
				.description(OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.hd_haptics.tooltip"))
						.text(hdHapticsNotSupported ? notSupportedText(Component.translatable("controlify.gui.hd_haptics")) : Component.empty())
						.build())
				.binding(defaults.hdHaptic.enabled, () -> settings.hdHaptic.enabled, v -> settings.hdHaptic.enabled = v)
				.controller(TickBoxControllerBuilder::create)
				.build());

		for (FeedbackSource source : Controlify.instance().feedbackSourceManager().sources()) {
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
			ControllerImpl testController = controller.get();
			vibrationGroup.option(ButtonOption.createBuilder()
					.name(Component.translatable("controlify.gui.test_vibration"))
					.description(OptionDescription.of(Component.translatable("controlify.gui.test_vibration.tooltip")))
					.action((screen, btn) -> {
						FeedbackEffect<Object> effect = FeedbackRegistryImpl.INSTANCE.findData(BuiltinFeedback.TEST_VIBRATION);
						if (effect != null) {
							testController.feedback().play(
									BuiltinFeedback.TEST_VIBRATION,
									FeedbackSource.MASTER,
									// stops as soon as the settings screen is left
									(playback, out) -> MinecraftUtil.getScreen() == screen && effect.sample(playback, out),
									null, 0f
							);
						}
					})
					.build());
		}

		return Optional.of(vibrationGroup.build());
	}

	private List<OptionGroup> makeGyroGroups(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		GyroSettings gyro = settings.gyro;
		GyroSettings def = defaults.gyro;
		boolean noGyro = controller.isPresent() && controller.get().gyro().isEmpty();

		// everything but the enable toggle is only available while gyro is enabled
		List<Option<?>> dependents = new ArrayList<>();

		ValueFormatter<Float> degreesPerSecond = v -> Component.translatable("controlify.gui.format.degrees_per_second", Math.round(v));
		ValueFormatter<Float> degreesPerSecondOrOff = v -> v == 0 ? CommonComponents.OPTION_OFF : degreesPerSecond.format(v);
		ValueFormatter<Float> degreesPerSecondOneDecimalOrOff = v -> v == 0
				? CommonComponents.OPTION_OFF
				: Component.translatable("controlify.gui.format.degrees_per_second", String.format(Locale.ROOT, "%.1f", v));
		ValueFormatter<Float> secondsAsMillis = v -> Component.literal(Math.round(v * 1000) + "ms");

		// activation
		var activationGroup = OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.group.gyro"))
				.description(OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.group.gyro.tooltip"))
						.build());
		if (noGyro) {
			activationGroup.collapsed(true);
			activationGroup.option(LabelOption.create(Component.translatable("controlify.gui.group.gyro.no_gyro.tooltip").withStyle(ChatFormatting.RED)));
		}

		activationGroup.option(Option.<Boolean>createBuilder()
				.name(Component.translatable("controlify.gui.gyro_enabled"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.gyro_enabled.tooltip")))
				.binding(def.enabled, () -> gyro.enabled, v -> gyro.enabled = v)
				.controller(TickBoxControllerBuilder::create)
				.addListener((opt, event) -> dependents.forEach(o -> o.setAvailable(opt.pendingValue())))
				.build());
		activationGroup.option(dependent(dependents, Option.<GyroActivation>createBuilder()
				.name(Component.translatable("controlify.gui.gyro_activation"))
				.description(val -> OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.gyro_activation.tooltip"))
						.text(Component.translatable("controlify.gui.gyro_activation.tooltip." + val.name().toLowerCase()))
						.build())
				.binding(def.activation, () -> gyro.activation, v -> gyro.activation = v)
				.controller(opt -> EnumControllerBuilder.create(opt).enumClass(GyroActivation.class))
				.available(gyro.enabled)
				.build()));
		activationGroup.option(dependent(dependents, Option.<Boolean>createBuilder()
				.name(Component.translatable("controlify.gui.gyro_active_while_aiming"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.gyro_active_while_aiming.tooltip")))
				.binding(def.activeWhileAiming, () -> gyro.activeWhileAiming, v -> gyro.activeWhileAiming = v)
				.controller(TickBoxControllerBuilder::create)
				.available(gyro.enabled)
				.build()));
		controller.flatMap(ControllerImpl::capSense).ifPresent(capSense -> {
			for (Identifier sensor : capSense.sensors().stream().sorted().toList()) {
				activationGroup.option(dependent(dependents, Option.<Boolean>createBuilder()
						.name(Component.translatableWithFallback(
								"controlify.gui.gyro_touch_activator." + sensor.getPath().replace('/', '.'),
								sensor.toString()
						))
						.description(OptionDescription.of(Component.translatable("controlify.gui.gyro_touch_activator.tooltip")))
						.binding(
								def.touchActivators.contains(sensor),
								() -> gyro.touchActivators.contains(sensor),
								v -> {
									if (v) {
										gyro.touchActivators.add(sensor);
									} else {
										gyro.touchActivators.remove(sensor);
									}
								}
						)
						.controller(TickBoxControllerBuilder::create)
						.available(gyro.enabled)
						.build()));
			}
		});
		activationGroup.option(dependent(dependents, Option.<Boolean>createBuilder()
				.name(Component.translatable("controlify.gui.gyro_invert_x"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.gyro_invert_x.tooltip")))
				.binding(def.invertYaw, () -> gyro.invertYaw, v -> gyro.invertYaw = v)
				.controller(TickBoxControllerBuilder::create)
				.available(gyro.enabled)
				.build()));
		activationGroup.option(dependent(dependents, Option.<Boolean>createBuilder()
				.name(Component.translatable("controlify.gui.gyro_invert_y"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.gyro_invert_y.tooltip")))
				.binding(def.invertPitch, () -> gyro.invertPitch, v -> gyro.invertPitch = v)
				.controller(TickBoxControllerBuilder::create)
				.available(gyro.enabled)
				.build()));

		// space and sensitivity
		var sensitivityGroup = OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.group.gyro_sensitivity"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.group.gyro_sensitivity.tooltip")))
				.collapsed(noGyro);
		sensitivityGroup.option(dependent(dependents, Option.<GyroSpace>createBuilder()
				.name(Component.translatable("controlify.gui.gyro_space"))
				.description(val -> OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.gyro_space.tooltip"))
						.text(Component.translatable("controlify.gui.gyro_space.tooltip." + val.name().toLowerCase()))
						.build())
				.binding(def.space, () -> gyro.space, v -> gyro.space = v)
				.controller(opt -> EnumControllerBuilder.create(opt).enumClass(GyroSpace.class))
				.available(gyro.enabled)
				.build()));
		sensitivityGroup.option(dependent(dependents, slider(
				"gyro_sensitivity", def.sensitivity, () -> gyro.sensitivity, v -> gyro.sensitivity = v,
				0.05f, 4f, 0.05f, percentFormatter, gyro.enabled
		)));
		sensitivityGroup.option(dependent(dependents, slider(
				"gyro_vertical_ratio", def.verticalRatio, () -> gyro.verticalRatio, v -> gyro.verticalRatio = v,
				0.25f, 2f, 0.05f, percentFormatter, gyro.enabled
		)));
		sensitivityGroup.option(dependent(dependents, slider(
				"gyro_aim_multiplier", def.aimMultiplier, () -> gyro.aimMultiplier, v -> gyro.aimMultiplier = v,
				0.1f, 1.5f, 0.05f, percentFormatter, gyro.enabled
		)));

		// acceleration
		var accelerationGroup = OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.group.gyro_acceleration"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.group.gyro_acceleration.tooltip")))
				.collapsed(true);
		accelerationGroup.option(dependent(dependents, slider(
				"gyro_fast_sensitivity", def.fastSensitivity, () -> gyro.fastSensitivity, v -> gyro.fastSensitivity = v,
				0.05f, 8f, 0.05f, percentFormatter, gyro.enabled
		)));
		accelerationGroup.option(dependent(dependents, slider(
				"gyro_accel_slow_threshold", def.accelSlowThreshold, () -> gyro.accelSlowThreshold, v -> gyro.accelSlowThreshold = v,
				0f, 200f, 5f, degreesPerSecond, gyro.enabled
		)));
		accelerationGroup.option(dependent(dependents, slider(
				"gyro_accel_fast_threshold", def.accelFastThreshold, () -> gyro.accelFastThreshold, v -> gyro.accelFastThreshold = v,
				0f, 800f, 10f, degreesPerSecond, gyro.enabled
		)));

		// smoothing and tightening
		var filteringGroup = OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.group.gyro_filtering"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.group.gyro_filtering.tooltip")))
				.collapsed(true);
		filteringGroup.option(dependent(dependents, slider(
				"gyro_smooth_threshold", def.smoothThreshold, () -> gyro.smoothThreshold, v -> gyro.smoothThreshold = v,
				0f, 100f, 1f, degreesPerSecondOrOff, gyro.enabled
		)));
		filteringGroup.option(dependent(dependents, slider(
				"gyro_smooth_time", def.smoothTime, () -> gyro.smoothTime, v -> gyro.smoothTime = v,
				0.01f, 0.5f, 0.01f, secondsAsMillis, gyro.enabled
		)));
		filteringGroup.option(dependent(dependents, slider(
				"gyro_tighten_threshold", def.tightenThreshold, () -> gyro.tightenThreshold, v -> gyro.tightenThreshold = v,
				0f, 20f, 0.5f, degreesPerSecondOneDecimalOrOff, gyro.enabled
		)));

		return List.of(activationGroup.build(), sensitivityGroup.build(), accelerationGroup.build(), filteringGroup.build());
	}

	private Optional<OptionGroup> makeFlickStickGroup(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {
		FlickStickSettings flick = settings.flickStick;
		FlickStickSettings def = defaults.flickStick;

		List<Option<?>> dependents = new ArrayList<>();

		var group = OptionGroup.createBuilder()
				.name(Component.translatable("controlify.gui.group.flick_stick"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.group.flick_stick.tooltip")));

		group.option(Option.<Boolean>createBuilder()
				.name(Component.translatable("controlify.gui.flick_stick"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.flick_stick.tooltip")))
				.binding(def.enabled, () -> flick.enabled, v -> flick.enabled = v)
				.controller(TickBoxControllerBuilder::create)
				.addListener((opt, event) -> dependents.forEach(o -> o.setAvailable(opt.pendingValue())))
				.build());
		group.option(dependent(dependents, slider(
				"flick_stick_time", def.flickTime, () -> flick.flickTime, v -> flick.flickTime = v,
				0.05f, 1f, 0.05f, v -> Component.literal(Math.round(v * 1000) + "ms"), flick.enabled
		)));
		group.option(dependent(dependents, slider(
				"flick_stick_forward_deadzone", def.forwardDeadzone, () -> flick.forwardDeadzone, v -> flick.forwardDeadzone = v,
				0f, 45f, 1f,
				v -> v == 0 ? CommonComponents.OPTION_OFF : Component.translatable("controlify.gui.format.degrees", Math.round(v)),
				flick.enabled
		)));
		group.option(dependent(dependents, Option.<FlickSnap>createBuilder()
				.name(Component.translatable("controlify.gui.flick_stick_snap"))
				.description(val -> OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.flick_stick_snap.tooltip"))
						.text(Component.translatable("controlify.gui.flick_stick_snap.tooltip." + val.name().toLowerCase()))
						.build())
				.binding(def.snap, () -> flick.snap, v -> flick.snap = v)
				.controller(opt -> EnumControllerBuilder.create(opt).enumClass(FlickSnap.class))
				.available(flick.enabled)
				.build()));
		group.option(dependent(dependents, Option.<Boolean>createBuilder()
				.name(Component.translatable("controlify.gui.flick_stick_rotate"))
				.description(OptionDescription.of(Component.translatable("controlify.gui.flick_stick_rotate.tooltip")))
				.binding(def.rotate, () -> flick.rotate, v -> flick.rotate = v)
				.controller(TickBoxControllerBuilder::create)
				.available(flick.enabled)
				.build()));

		return Optional.of(group.build());
	}

	private static <T> Option<T> dependent(List<Option<?>> dependents, Option<T> option) {
		dependents.add(option);
		return option;
	}

	private static Option<Float> slider(
			String key,
			float defaultValue,
			Supplier<Float> getter,
			Consumer<Float> setter,
			float min,
			float max,
			float step,
			ValueFormatter<Float> formatter,
			boolean available
	) {
		return Option.<Float>createBuilder()
				.name(Component.translatable("controlify.gui." + key))
				.description(OptionDescription.of(Component.translatable("controlify.gui." + key + ".tooltip")))
				.binding(defaultValue, getter, setter)
				.controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step).formatValue(formatter))
				.available(available)
				.build();
	}

	private Optional<OptionGroup> makeTriggerEffectsGroup(
		ProfileSettings settings,
		ProfileSettings defaults,
		Optional<ControllerImpl> controller
	) {
		var group = OptionGroup.createBuilder()
			.name(Component.translatable("controlify.gui.group.trigger_effects"))
			.description(OptionDescription.createBuilder()
				.text(Component.translatable("controlify.gui.trigger_effects.tooltip"))
				.build());

		if (controller.isPresent() && controller.get().dualsense().isEmpty()) {
			group.collapsed(true);
			group.option(LabelOption.create(Component.translatable("controlify.gui.group.trigger_effects.no_dualsense.tooltip").withStyle(ChatFormatting.RED)));
		}

		group.option(Option.<Boolean>createBuilder()
			.name(Component.translatable("controlify.gui.trigger_effects"))
			.description(OptionDescription.of(Component.translatable("controlify.gui.trigger_effects.tooltip")))
			.binding(defaults.dualsense.triggerEffects, () -> settings.dualsense.triggerEffects, v -> settings.dualsense.triggerEffects = v)
			.controller(TickBoxControllerBuilder::create)
			.build());

		return Optional.of(group.build());
	}

	private Optional<ConfigCategory> makeBindsCategory(
			ProfileSettings settings,
			ProfileSettings defaults,
			Optional<ControllerImpl> controller
	) {

		var category = ConfigCategory.createBuilder()
				.name(Component.translatable("controlify.gui.config.category.bindings"));

		InputSettings.RadialMenuSettings radialConfig = settings.input.radialMenu;
		InputSettings.RadialMenuSettings radialDef = defaults.input.radialMenu;

		List<OptionActionPair> optionActions = new ArrayList<>();

		category
				.optionIf(controller.isPresent(), ButtonOption.createBuilder()
						.name(Component.translatable("controlify.gui.radial_menu").withStyle(ChatFormatting.GOLD))
						.description(OptionDescription.createBuilder()
								.text(Component.translatable("controlify.gui.radial_menu.tooltip"))
								.build())
						.action((screen, opt) -> {
							MinecraftUtil.setScreen(new RadialMenuEditScreen(screen, controller.orElseThrow()));
						})
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

		Collection<Action> allActions = controller
				.<Collection<Action>>map(c -> c.input().getAllActions().stream()
						.filter(action -> action.spec().shouldExpose().test(c))
						.map(action -> (Action) action).toList())
				.orElseGet(() -> ActionSpecRegistryImpl.INSTANCE.getActionSpecs().stream()
						.map(spec -> {
							List<ActionBinding> defaultBindings = Controlify.instance().defaultBindManager()
									.getDefaultBindProvider(ControllerType.DEFAULT.id()).getDefaultBindings(spec.id());
							var action = new ActionImpl(
									spec,
									settings.input.bindings.resolve(spec.id(), defaultBindings), defaultBindings,
									Controlify.instance().activatorConfigManager().getConfig(spec.id()),
									new ActionDispatcher()
							);
							action.reset();
							return (Action) action;
						}).toList());

		groupActions(allActions).forEach((categoryName, bindGroup) -> {
			var controlsGroup = OptionGroup.createBuilder()
					.name(categoryName);

			for (Action action : bindGroup) {
				Option<List<ActionBinding>> built = createActionOpt(action, settings, controller.orElse(null))
						.addListener((opt, val) -> updateConflictingActions(optionActions))
						.build();
				optionActions.add(new OptionActionPair(built, (ActionImpl) action));
				controlsGroup.option(built);
			}

			category.group(controlsGroup.build());
		});
		updateConflictingActions(optionActions);

		category.option(ButtonOption.createBuilder()
				.name(Component.translatable("controlify.gui.reset_all_binds"))
				.description(OptionDescription.createBuilder()
						.text(Component.translatable("controlify.gui.reset_all_binds.tooltip"))
						.build())
				.action((screen, opt) -> {
					for (OptionActionPair pair : optionActions) {
						pair.option().requestSet(pair.action().defaultBindings());
					}
				})
				.build());

		return Optional.of(category.build());
	}

	private void updateConflictingActions(List<OptionActionPair> all) {
		Map<Identifier, OptionActionPair> byAction = new HashMap<>();
		List<BindingConflicts.Entry> entries = new ArrayList<>();
		for (OptionActionPair pair : all) {
			byAction.put(pair.action().spec().id(), pair);
			entries.add(new BindingConflicts.Entry(pair.action().spec().id(), pair.action().spec().contexts(),
					pair.option().pendingValue(), pair.action().activatorConfig()));
		}

		Map<OptionActionPair, BindingConflicts.Kind[]> worst = new HashMap<>();
		Map<OptionActionPair, List<BindingConflicts.Conflict>> byPair = new HashMap<>();
		for (BindingConflicts.Conflict conflict : BindingConflicts.find(entries, ActionLayerRegistryImpl.INSTANCE.getOrderedContexts())) {
			OptionActionPair pair = byAction.get(conflict.action());
			byPair.computeIfAbsent(pair, _ -> new ArrayList<>()).add(conflict);
			if (conflict.bindingIndex() < BindController.SLOTS) {
				var kinds = worst.computeIfAbsent(pair, _ -> new BindingConflicts.Kind[BindController.SLOTS]);
				BindingConflicts.Kind current = kinds[conflict.bindingIndex()];
				// Kinds are declared most severe first.
				if (current == null || conflict.kind().ordinal() < current.ordinal()) {
					kinds[conflict.bindingIndex()] = conflict.kind();
				}
			}
		}
		for (OptionActionPair pair : all) {
			((BindController) pair.option().controller()).setConflicts(
					worst.getOrDefault(pair, new BindingConflicts.Kind[BindController.SLOTS]),
					describeBindings(pair, byPair.getOrDefault(pair, List.of()), byAction));
		}
	}

	/// One heading per binding, naming its gesture, followed by each of its conflicts, most severe first.
	private List<Component> describeBindings(
			OptionActionPair pair, List<BindingConflicts.Conflict> conflicts, Map<Identifier, OptionActionPair> byAction
	) {
		List<Component> lines = new ArrayList<>();
		List<ActionBinding> bindings = pair.option().pendingValue();
		for (int index = 0; index < bindings.size(); index++) {
			int bindingIndex = index;
			List<BindingConflicts.Conflict> forBinding = conflicts.stream()
					.filter(conflict -> conflict.bindingIndex() == bindingIndex)
					.sorted(Comparator.comparing(BindingConflicts.Conflict::kind))
					.toList();
			ActionBinding binding = bindings.get(index);
			MutableComponent described = Component.empty().append(bindingGlyph(pair, binding));
			// Spelling the gesture out beside its badge teaches what each badge means.
			pair.action().activatorConfig().displayGesture(binding)
					.filter(gesture -> Controlify.instance().inputFontMapper().hasGestureBadge(fontNamespace(pair), gesture))
					.ifPresent(gesture -> described.append(" ").append(
							Component.translatable("controlify.gesture." + gesture.getSerializedName())
									.withStyle(ChatFormatting.GRAY)));
			lines.add(Component.translatable("controlify.gui.bind.binding", index + 1, described));
			for (BindingConflicts.Conflict conflict : forBinding) {
				lines.add(Component.literal(" • ").append(describeConflict(conflict, byAction.get(conflict.other()))));
			}
		}
		return lines;
	}

	private Component bindingGlyph(OptionActionPair pair, ActionBinding binding) {
		return Controlify.instance().inputFontMapper()
				.getComponentFromBinding(fontNamespace(pair), binding, pair.action().activatorConfig().displayGesture(binding));
	}

	private static Identifier fontNamespace(OptionActionPair pair) {
		ControllerImpl controller = ((BindController) pair.option().controller()).controller;
		return controller == null ? ControllerType.DEFAULT.id() : controller.info().type().id();
	}

	private Component describeConflict(BindingConflicts.Conflict conflict, OptionActionPair other) {
		String key = "controlify.gui.bind.conflict." + conflict.kind().name().toLowerCase(Locale.ROOT);
		ChatFormatting colour = switch (conflict.kind()) {
			case CONFLICT -> ChatFormatting.RED;
			case MODIFIER_LEAK -> ChatFormatting.GOLD;
			case REDUNDANT, SHADOWED -> ChatFormatting.GRAY;
		};
		if (conflict.kind() == BindingConflicts.Kind.REDUNDANT) {
			return Component.translatable(key, conflict.otherBindingIndex() + 1).withStyle(colour);
		}
		Component otherBinding = bindingGlyph(other, other.option().pendingValue().get(conflict.otherBindingIndex()));
		return Component.translatable(key, other.action().spec().name(), otherBinding).withStyle(colour);
	}

	private static Map<Component, List<Action>> groupActions(Collection<Action> bindings) {
		return bindings.stream()
				.collect(Collectors.groupingBy(a -> a.spec().category(), LinkedHashMap::new, Collectors.toList()));
	}

	private Option.Builder<List<ActionBinding>> createActionOpt(Action action, ProfileSettings settings, @Nullable ControllerImpl controller) {
		Identifier fontNamespace = controller == null
				? ControllerType.DEFAULT.id()
				: controller.info().type().id();
		ActivatorConfig<?> activator = ((ActionImpl) action).activatorConfig();
		var fontMapper = Controlify.instance().inputFontMapper();
		// The description is built before the controller exists, but renders after it.
		AtomicReference<BindController> bindController = new AtomicReference<>();
		AtomicReference<Option<List<ActionBinding>>> optionRef = new AtomicReference<>();

		var description = OptionDescription.createBuilder()
				.text(action.spec().description());
		if (activator.editableLeaf().isPresent()) {
			description.text(Component.translatable("controlify.gui.bind.gesture_hint",
					ActionGlyphs.action(ControlifyActions.GUI_ABSTRACT_ACTION_2)).withStyle(ChatFormatting.AQUA));
		}
		description.customImage(new BindingInfoImageRenderer(() -> {
			List<Component> lines = new ArrayList<>();
			Option<List<ActionBinding>> option = optionRef.get();
			if (option != null && !option.pendingValue().equals(action.defaultBindings())) {
				Component defaults = action.defaultBindings().isEmpty()
						? fontMapper.getComponentFromInput(fontNamespace, null)
						: ComponentUtils.formatList(action.defaultBindings(), Component.literal(", "),
								binding -> fontMapper.getComponentFromBinding(fontNamespace, binding, activator.displayGesture(binding)));
				lines.add(Component.translatable("controlify.gui.bind.default_bound_to", defaults));
			}
			BindController controllerInstance = bindController.get();
			if (controllerInstance != null) {
				lines.addAll(controllerInstance.bindingLines());
			}
			return lines;
		}));

		return Option.<List<ActionBinding>>createBuilder()
				.name(action.spec().name())
				.description(description.build())
				.binding(action.defaultBindings(), action::bindings, bindings -> {
					action.setBindings(bindings);
					if (bindings.equals(action.defaultBindings())) {
						settings.input.bindings.bindings.remove(action.spec().id());
					} else {
						settings.input.bindings.bindings.put(action.spec().id(), bindings);
					}
				})
				.customController(opt -> {
					optionRef.set(opt);
					var created = new BindController(opt, controller, activator);
					bindController.set(created);
					return created;
				});
	}

	private static Identifier screenshot(String filename) {
		return CUtil.rl("textures/screenshots/" + filename);
	}

	private static MutableComponent notSupportedText(Component featureName) {
		return Component.translatable("controlify.gui.not_supported", featureName).withStyle(ChatFormatting.RED);
	}

	private record OptionActionPair(Option<List<ActionBinding>> option, ActionImpl action) {
	}
}
