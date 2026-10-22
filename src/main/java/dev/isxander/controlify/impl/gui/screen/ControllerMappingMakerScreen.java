/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.screen;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.config.settings.device.DeviceSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.input.mapping.ControllerMapping;
import dev.isxander.controlify.impl.input.mapping.DeadzoneGroup;
import dev.isxander.controlify.impl.input.mapping.GamepadInputs;
import dev.isxander.controlify.impl.input.mapping.MapType;
import dev.isxander.controlify.impl.input.mapping.MappingEntry;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.ColorUtils;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.impl.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ControllerMappingMakerScreen extends Screen implements ScreenProcessorProvider, DontInterruptScreen {
	private static final float AXIS_PRESS_THRESHOLD = 0.5f;
	private static final float AXIS_RELEASE_THRESHOLD = 0.25f;

	private final ControllerImpl controller;
	private final Map<Identifier, MappingEntry> capturedMappings = new LinkedHashMap<>();
	private final Set<Identifier> activeInputs = new HashSet<>();
	private EventSource.Subscription inputSubscription;
	private final ControllerMapping.Builder mappingBuilder = new ControllerMapping.Builder();
	private final ScreenProcessor<ControllerMappingMakerScreen> screenProcessor = new ScreenProcessorImpl(this);

	private int delayTillNextStage = 20;

	public static final List<MappingStage> GAMEPAD_STAGES = List.of(
			new MappingStage(GamepadInputs.SOUTH_BUTTON, MapType.BUTTON, button("face_down"), "face_down", "faceview"),
			new MappingStage(GamepadInputs.WEST_BUTTON, MapType.BUTTON, button("face_left"), "face_left", "faceview"),
			new MappingStage(GamepadInputs.EAST_BUTTON, MapType.BUTTON, button("face_right"), "face_right", "faceview"),
			new MappingStage(GamepadInputs.NORTH_BUTTON, MapType.BUTTON, button("face_up"), "face_up", "faceview"),
			new MappingStage(GamepadInputs.LEFT_SHOULDER_BUTTON, MapType.BUTTON, button("left_bumper"), "left_bumper", "triggerview"),
			new MappingStage(GamepadInputs.RIGHT_SHOULDER_BUTTON, MapType.BUTTON, button("right_bumper"), "right_bumper", "triggerview"),
			new MappingStage(GamepadInputs.BACK_BUTTON, MapType.BUTTON, button("left_special"), "left_special", "faceview"),
			new MappingStage(GamepadInputs.START_BUTTON, MapType.BUTTON, button("right_special"), "right_special", "faceview"),
			new MappingStage(GamepadInputs.LEFT_STICK_BUTTON, MapType.BUTTON, button("left_stick_down"), "left_stick_press", "faceview"),
			new MappingStage(GamepadInputs.RIGHT_STICK_BUTTON, MapType.BUTTON, button("right_stick_down"), "right_stick_press", "faceview"),
			new MappingStage(GamepadInputs.DPAD_UP_BUTTON, MapType.BUTTON, button("dpad_up"), "dpad_up", "faceview"),
			new MappingStage(GamepadInputs.DPAD_LEFT_BUTTON, MapType.BUTTON, button("dpad_left"), "dpad_left", "faceview"),
			new MappingStage(GamepadInputs.DPAD_DOWN_BUTTON, MapType.BUTTON, button("dpad_down"), "dpad_down", "faceview"),
			new MappingStage(GamepadInputs.DPAD_RIGHT_BUTTON, MapType.BUTTON, button("dpad_right"), "dpad_right", "faceview"),
			new MappingStage(GamepadInputs.LEFT_STICK_AXIS_LEFT, MapType.AXIS, axis("left_stick", "left"), "left_stick_left", "faceview"),
			new MappingStage(GamepadInputs.LEFT_STICK_AXIS_DOWN, MapType.AXIS, axis("left_stick", "down"), "left_stick_down", "faceview"),
			new MappingStage(GamepadInputs.LEFT_STICK_AXIS_RIGHT, MapType.AXIS, axis("left_stick", "right"), "left_stick_right", "faceview"),
			new MappingStage(GamepadInputs.LEFT_STICK_AXIS_UP, MapType.AXIS, axis("left_stick", "up"), "left_stick_up", "faceview"),
			new MappingStage(GamepadInputs.RIGHT_STICK_AXIS_LEFT, MapType.AXIS, axis("right_stick", "left"), "right_stick_left", "faceview"),
			new MappingStage(GamepadInputs.RIGHT_STICK_AXIS_DOWN, MapType.AXIS, axis("right_stick", "down"), "right_stick_down", "faceview"),
			new MappingStage(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, MapType.AXIS, axis("right_stick", "right"), "right_stick_right", "faceview"),
			new MappingStage(GamepadInputs.RIGHT_STICK_AXIS_UP, MapType.AXIS, axis("right_stick", "up"), "right_stick_up", "faceview"),
			new MappingStage(GamepadInputs.LEFT_TRIGGER_AXIS, MapType.AXIS, Component.translatable("controlify.gui.mapping_maker.instruction.left_trigger"), "left_trigger", "triggerview"),
			new MappingStage(GamepadInputs.RIGHT_TRIGGER_AXIS, MapType.AXIS, Component.translatable("controlify.gui.mapping_maker.instruction.right_trigger"), "right_trigger", "triggerview")
	);

	private MappingStage currentStage = null;
	private final List<MappingStage> stages;

	private Button goBackButton;
	private final Screen lastScreen;

	public ControllerMappingMakerScreen(ControllerImpl controller, Screen lastScreen, List<MappingStage> stages, Iterable<DeadzoneGroup> deadzoneGroups) {
		super(Component.literal("Gamepad Emulation Mapping Creator"));
		this.controller = controller;
		this.lastScreen = lastScreen;
		this.stages = stages.stream().map(MappingStage::new).toList();
		this.mappingBuilder.putDeadzoneGroups(deadzoneGroups);
	}

	public static ControllerMappingMakerScreen createGamepadMapping(ControllerImpl controller, Screen lastScreen) {
		return new ControllerMappingMakerScreen(controller, lastScreen, GAMEPAD_STAGES, GamepadInputs.DEADZONE_GROUPS);
	}

	@Override
	protected void init() {
		addRenderableWidget(
				this.goBackButton = Button.builder(
						Component.translatable("controlify.gui.mapping_maker.go_back"),
						button -> goBackStage()
				)
						.bounds(width / 2 - 152, height - 60, 150, 20)
						.tooltip(Tooltip.create(Component.translatable("controlify.gui.mapping_maker.go_back.tooltip")))
						.build()
		);
		addRenderableWidget(
				Button.builder(
						Component.translatable("controlify.gui.mapping_maker.no_map"),
						button -> mapAsNone()
				)
						.bounds(width / 2 + 2, height - 60, 150, 20)
						.tooltip(Tooltip.create(Component.translatable("controlify.gui.mapping_maker.no_map.tooltip")))
						.build()
		);
		goBackButton.active = currentStage != null && stages.indexOf(currentStage) > 0;
	}

	@Override
	public void tick() {
		if (delayTillNextStage < 0 || delayTillNextStage-- > 0) {
			return;
		}

		int nextIndex = currentStage == null ? 0 : stages.indexOf(currentStage) + 1;
		if (nextIndex >= stages.size()) {
			onClose();
			return;
		}
		this.setStage(stages.get(nextIndex));
		if (currentStage.isSatisfied()) {
			// A later step may already be mapped when revisiting an earlier one.
			delayTillNextStage = 20;
		}
	}

	@Override
	public void added() {
		if (this.inputSubscription != null) {
			this.inputSubscription.close();
		}
		// Capture original source IDs before mappings, deadzones, gestures, and domain expansion.
		this.inputSubscription = this.controller.input().inputPipeline()
				.sourceRawPhysicalSignals().subscribe(this::onPhysicalSignal);
	}

	@Override
	public void removed() {
		if (this.inputSubscription != null) {
			this.inputSubscription.close();
			this.inputSubscription = null;
		}
	}

	private void onPhysicalSignal(PhysicalSignal signal) {
		Identifier input;
		MapType inputType;
		boolean pressed;
		switch (signal) {
			case PhysicalInputSignal.ButtonChanged button -> {
				input = button.input();
				inputType = MapType.BUTTON;
				pressed = button.state();
			}
			case PhysicalInputSignal.AxisMoved axis -> {
				input = axis.input();
				inputType = MapType.AXIS;
				// Hysteresis prevents stick noise around the capture threshold from retriggering.
				pressed = axis.value() >= (this.activeInputs.contains(input)
						? AXIS_RELEASE_THRESHOLD : AXIS_PRESS_THRESHOLD);
			}
			default -> {
				return;
			}
		}

		// Track releases during the cooldown too, so held inputs cannot satisfy the next step.
		if (!pressed) {
			this.activeInputs.remove(input);
			return;
		}
		if (!this.activeInputs.add(input) || this.delayTillNextStage >= 0
				|| this.currentStage == null || this.currentStage.isSatisfied()) {
			return;
		}
		// MappingStage requires each physical source to have a single destination.
		if (this.capturedMappings.values().stream().anyMatch(mapping -> mapping.sourceInput().equals(input))) {
			return;
		}

		Identifier output = this.currentStage.originInput();
		MappingEntry mapping = switch (inputType) {
			case BUTTON -> switch (this.currentStage.outputType()) {
				case BUTTON -> new MappingEntry.FromButton.ToButton(input, output, false);
				case AXIS -> new MappingEntry.FromButton.ToAxis(input, output, 0, 1);
			};
			case AXIS -> switch (this.currentStage.outputType()) {
				case BUTTON -> new MappingEntry.FromAxis.ToButton(input, output, AXIS_PRESS_THRESHOLD);
				case AXIS -> new MappingEntry.FromAxis.ToAxis(input, output, 0, 0, 1, 1);
			};
		};
		this.capturedMappings.put(output, mapping);
		this.currentStage.setSatisfied(true);
		this.delayTillNextStage = 20;
	}

	private void setStage(MappingStage stage) {
		currentStage = stage;
		goBackButton.active = stages.indexOf(stage) > 0;
	}

	private void mapAsNone() {
		if (currentStage == null) {
			return;
		}
		capturedMappings.remove(currentStage.originInput());
		currentStage.setSatisfied(false);
		delayTillNextStage = 0;
	}

	@Override
	public void onClose() {
		MinecraftUtil.setScreen(lastScreen);
		DeviceSettings deviceSettings = Controlify.instance().config().getSettings()
				.getOrCreateDeviceSettings(controller.uid());
		capturedMappings.values().forEach(mappingBuilder::putMapping);
		deviceSettings.mapping = mappingBuilder.build();
		controller.input().reloadControllerMapping();
		Controlify.instance().config().saveSafely();
	}

	private void goBackStage() {
		if (currentStage == null) {
			return;
		}
		int index = stages.indexOf(currentStage);
		int nextIndex = index - 1;
		if (nextIndex >= 0) {
			this.setStage(stages.get(index - 1));
			currentStage.setSatisfied(false);
			capturedMappings.remove(currentStage.originInput());
			delayTillNextStage = -1;
		}
		goBackButton.active = nextIndex > 0;
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);

		graphics.centeredText(font, Component.translatable("controlify.gui.mapping_maker.title"), width / 2, 15, 0xFFFFFFFF);

		graphics.centeredText(
				font,
				currentStage == null ? Component.translatable("controlify.gui.mapping_maker.please_wait") : currentStage.name(),
				width / 2, height - 20,
				0xFFFFFFFF
		);

		int safeZone = Math.min(width, height) - 30;
		float scale = safeZone / 32f;

		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(width / 2f, -5);
		pose.translate(-32 * scale / 2f, 0);
		pose.scale(scale, scale);

		float colour = currentStage != null && currentStage.isSatisfied() ? 0.46f : 1f;

		if (currentStage != null && currentStage.background() != null) {
			Identifier texture = currentStage.background();
			int color = ColorUtils.grey(colour, 1f);
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					texture,
					0, 0,
					(float) 0, (float) 0,
					32, 32,
					32, 32,
					color
			);
		}

		if (currentStage == null || !currentStage.isSatisfied()) {
			Identifier texture = currentStage != null ? currentStage.foreground() : CUtil.rl("textures/gui/controllerdiagram/faceview.png");
			int color = ColorUtils.grey(colour, 1f);
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					texture,
					0, 0,
					(float) 0, (float) 0,
					32, 32,
					32, 32,
					color
			);
		}

		pose.popMatrix();

		float progress = currentStage != null ? (float) (stages.indexOf(currentStage) + 1) / stages.size() : 0;
		RenderUtils.extractBar(graphics, width / 2, height - 30, progress);
	}

	private static Component button(String buttonName) {
		return Component.translatable("controlify.gui.mapping_maker.instruction.button", Component.translatable("controlify.gui.mapping_maker.instruction." + buttonName));
	}

	private static Component axis(String axisName, String direction) {
		var axisText = Component.translatable("controlify.gui.mapping_maker.instruction." + axisName);
		var directionText = Component.translatable("controlify.gui.mapping_maker.instruction.axis_" + direction);
		return Component.translatable("controlify.gui.mapping_maker.instruction.axis", axisText, directionText);
	}

	@Override
	public ScreenProcessor<?> screenProcessor() {
		return screenProcessor;
	}

	public static class MappingStage {
		private final Identifier originInput;
		private final MapType outputType;
		private final Component name;
		private final Identifier foreground;
		private final Identifier background;
		private boolean satisfied;

		public MappingStage(Identifier originInput, MapType outputType, Component name, String foreground, String background) {
			this.originInput = originInput;
			this.outputType = outputType;
			this.name = name;
			this.foreground = CUtil.rl("textures/gui/controllerdiagram/" + foreground + ".png");
			this.background = CUtil.rl("textures/gui/controllerdiagram/" + background + ".png");
		}

		private MappingStage(MappingStage template) {
			this.originInput = template.originInput;
			this.outputType = template.outputType;
			this.name = template.name;
			this.foreground = template.foreground;
			this.background = template.background;
			this.satisfied = false;
		}

		public Identifier originInput() {
			return originInput;
		}

		public MapType outputType() {
			return this.outputType;
		}

		public Component name() {
			return name;
		}

		public Identifier foreground() {
			return foreground;
		}

		public Identifier background() {
			return background;
		}

		public boolean isSatisfied() {
			return satisfied;
		}

		public void setSatisfied(boolean satisfied) {
			this.satisfied = satisfied;
		}
	}

	private static class ScreenProcessorImpl extends ScreenProcessor<ControllerMappingMakerScreen> {
		public ScreenProcessorImpl(ControllerMappingMakerScreen screen) {
			super(screen);
		}

		@Override
		public void onControllerFixedUpdate(ControllerImpl controller) {
			// Captured physical inputs must not also activate screen controls.
		}
	}
}
