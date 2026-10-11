package dev.isxander.controlify.input;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.defaults.DefaultBindProvider;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.config.settings.profile.InputSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.controller.input.DeadzoneGroup;
import dev.isxander.controlify.input.action.ActionGraph;
import dev.isxander.controlify.input.action.ActionImpl;
import dev.isxander.controlify.input.action.ActionSpec;
import dev.isxander.controlify.input.action.ActionSpecRegistryImpl;
import dev.isxander.controlify.input.action.activator.ActivatorConfig;
import dev.isxander.controlify.input.logical.LogicalInput;
import dev.isxander.controlify.input.physical.SensorProcessorStage;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InputComponent extends ECSComponentImpl {
	public static final Identifier ID = CUtil.rl("input");

	private final Identifier controllerType;
	private DeviceSettings deviceSettings;

	private final InputPipeline inputPipeline;
	private final ActionGraph actionGraph;

	private final Map<Identifier, ActionImpl> actions;

	private final Set<Identifier> physicalButtons, physicalAxes;
	private final Map<Identifier, DeadzoneGroup> deadzoneGroups;
	private final boolean isGamepad;
	private final Set<SensorType> supportedSensors;

	public InputComponent(
			Identifier controllerType,
			InputPipeline inputPipeline,
			Set<Identifier> physicalButtons,
			Set<Identifier> physicalAxes,
			Set<DeadzoneGroup> deadzoneGroups,
			boolean isGamepad,
			Set<SensorType> supportedSensors
	) {
		this.controllerType = controllerType;
		this.inputPipeline = inputPipeline;

		this.physicalButtons = Set.copyOf(physicalButtons);
		this.physicalAxes = Set.copyOf(physicalAxes);
		this.deadzoneGroups = deadzoneGroups.stream()
				.flatMap(g -> g.axes().stream().map(a -> Map.entry(a, g)))
				.collect(Util.toMap());
		this.isGamepad = isGamepad;
		this.supportedSensors = Set.copyOf(supportedSensors);

		this.actionGraph = new ActionGraph();
		this.actions = new LinkedHashMap<>();
	}

	@Override
	public void attach(ControllerEntity controller) {
		super.attach(controller);

		this.deviceSettings = Controlify.instance().config().getSettings()
				.getOrCreateDeviceSettings(controller.uid());

		// we need to wait for settings to load before getting current binding
		this.reloadActions();

		this.inputPipeline.sourceLogicalSignals().subscribe(this.actionGraph);
		this.inputPipeline.stageDeadzone().setDeadzoneSupplier(axis -> {
			DeadzoneGroup group = this.deadzoneGroups.get(axis);
			if (group == null) {
				return 0f;
			}
			return this.controller().settings().input.sensitivity.getDeadzone(group.name());
		});
		this.inputPipeline.stageSensorProcessor().setProcessors(sensorType -> {
			if (sensorType.isGyroscope()) {
				return List.of(
						new SensorProcessorStage.BiasCorrection(() -> this.deviceSettings.gyroCalibration.offset),
						new SensorProcessorStage.LowPassFilter(() -> 15.0) // TODO: make this configurable
				);
			} else {
				return List.of();
			}
		});
	}

	public @Nullable ActionImpl getAction(Identifier actionId) {
		return this.actions.get(actionId);
	}

	public void reloadActions() {
		this.actions.clear();
		this.actionGraph.clearActions();

		for (ActionSpec spec : ActionSpecRegistryImpl.INSTANCE.getActionSpecs()) {
			ActionImpl action = this.createAction(spec);
			this.actions.put(spec.id(), action);
			this.actionGraph.addAction(action);
		}
	}

	public void onGuiNavigate() {
		this.inputPipeline.stagePhysicalToLogical().onGuiNavigate();
	}

	@Override
	public Identifier id() {
		return ID;
	}

	public InputSettings settings() {
		return this.controller().settings().input;
	}

	public InputSettings defaultSettings() {
		return this.controller().defaultSettings().input;
	}

	public boolean isGamepad() {
		return this.isGamepad;
	}

	public Set<Identifier> physicalButtons() {
		return this.physicalButtons;
	}

	public Set<Identifier> physicalAxes() {
		return this.physicalAxes;
	}

	public boolean supportsSensor(SensorType type) {
		return this.supportedSensors.contains(type);
	}

	public boolean supportsGyro() {
		return this.supportedSensors.stream().anyMatch(SensorType::isGyroscope);
	}

	public boolean supportsAccelerometer() {
		return this.supportedSensors.stream().anyMatch(SensorType::isAccelerometer);
	}

	private ActionImpl createAction(ActionSpec spec) {
		DefaultBindProvider bindProvider = Controlify.instance().defaultBindManager()
				.getDefaultBindProvider(this.controllerType);
		LogicalInput defaultBoundInput = bindProvider.getDefaultInput(spec.id());

		LogicalInput currentBoundInput = settings().bindings.bindings.get(spec.id());

		ActivatorConfig<?> activatorConfig = Controlify.instance().activatorConfigManager().getConfig(spec.id());

		ActionImpl action = new ActionImpl(
				spec,
				currentBoundInput,
				defaultBoundInput,
				activatorConfig,
				this.actionGraph
		);
		action.reset();

		return action;
	}
}
