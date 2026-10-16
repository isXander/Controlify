package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.action.defaults.DefaultBindProvider;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.config.settings.profile.InputSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.input.LogicalInput;
import dev.isxander.controlify.input.SensorType;
import dev.isxander.controlify.input.mapping.ControllerMapping;
import dev.isxander.controlify.input.mapping.ControllerMappingManager;
import dev.isxander.controlify.action.ActionGraph;
import dev.isxander.controlify.action.ActionImpl;
import dev.isxander.controlify.action.ActionSpec;
import dev.isxander.controlify.action.ActionSpecRegistryImpl;
import dev.isxander.controlify.action.activator.ActivatorConfig;
import dev.isxander.controlify.input.pipeline.InputPipeline;
import dev.isxander.controlify.input.pipeline.SensorProcessorStage;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.event.EventSource;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class InputComponent extends ECSComponentImpl {
	public static final Identifier ID = CUtil.rl("input");

	private final Identifier controllerType;
	private DeviceSettings deviceSettings;

	private final InputPipeline inputPipeline;
	private final ActionGraph actionGraph;

	private final Map<Identifier, ActionImpl> actions;

	private final Set<Identifier> physicalButtons, physicalAxes;
	private final boolean isGamepad;
	private final Set<SensorType> supportedSensors;

	private EventSource.Subscription actionGraphSub;

	public InputComponent(
			Identifier controllerType,
			InputPipeline inputPipeline,
			Set<Identifier> physicalButtons,
			Set<Identifier> physicalAxes,
			Set<SensorType> supportedSensors,
			boolean isGamepad
	) {
		this.controllerType = controllerType;
		this.inputPipeline = inputPipeline;

		this.physicalButtons = Set.copyOf(physicalButtons);
		this.physicalAxes = Set.copyOf(physicalAxes);
		this.supportedSensors = Set.copyOf(supportedSensors);
		this.isGamepad = isGamepad;

		this.actionGraph = new ActionGraph();
		this.actions = new LinkedHashMap<>();
	}

	@Override
	public void attach(ControllerEntity controller) {
		super.attach(controller);

		this.deviceSettings = Controlify.instance().config().getSettings()
				.getOrCreateDeviceSettings(controller.uid());

		ControllerMapping controllerMapping = this.deviceSettings.mapping;
		if (controllerMapping == null) {
			ControllerMappingManager controllerMappingManager = Controlify.instance().controllerMappingManager();
			controllerMapping = controllerMappingManager
					.getMappingById(controller.info().type().mappingId())
					.orElse(null);
		}
		if (controllerMapping == null) {
			controllerMapping = this.isGamepad
					? ControllerMapping.GAMEPAD_DEADZONES_ONLY_MAPPING
					: ControllerMapping.NO_MAPPING;
		}

		this.inputPipeline.stageMapping().setControllerMapping(controllerMapping);
		this.inputPipeline.stageDeadzone().setDeadzoneGroups(controllerMapping.deadzones().values());
		this.inputPipeline.stageDeadzone().setDeadzoneSupplier(axis -> settings().sensitivity.getDeadzone((Identifier) axis));
		this.inputPipeline.stageSensorProcessor().setProcessors(sensorType -> {
			if (sensorType == SensorType.GYROSCOPE) {
				return List.of(
						new SensorProcessorStage.BiasCorrection(() -> this.deviceSettings.gyroCalibration.offset),
						new SensorProcessorStage.LowPassFilter(() -> this.controller().settings().gyro.lowPassCutoffHz)
				);
			} else {
				return List.of();
			}
		});

		this.reloadActions();
		this.actionGraphSub = this.inputPipeline.sourceLogicalSignals().subscribe(this.actionGraph);
	}

	public @Nullable ActionImpl getAction(Identifier actionId) {
		return this.actions.get(actionId);
	}

	public InputPipeline inputPipeline() {
		return this.inputPipeline;
	}

	public void resetActions() {
		this.actions.values().forEach(ActionImpl::reset);
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

	@Override
	public void close() {
		super.close();

		this.actionGraphSub.close();
		this.actionGraphSub = null;
	}

	private ActionImpl createAction(ActionSpec spec) {
		DefaultBindProvider bindProvider = Controlify.instance().defaultBindManager()
				.getDefaultBindProvider(this.controllerType);
		LogicalInput defaultBoundInput = bindProvider.getDefaultInput(spec.id());

		LogicalInput currentBoundInput = settings().bindings.bindings.get(spec.id());

		ActivatorConfig<?> activatorConfig = Controlify.instance().activatorConfigManager()
				.getConfig(spec.id());

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
