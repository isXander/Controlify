/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.input;

import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.controller.input.InputComponent;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ActionDispatcher;
import dev.isxander.controlify.impl.action.ActionImpl;
import dev.isxander.controlify.impl.action.ActionSpecRegistryImpl;
import dev.isxander.controlify.impl.action.activator.ActivatorConfig;
import dev.isxander.controlify.impl.action.defaults.DefaultBindProvider;
import dev.isxander.controlify.impl.action.proxy.ActionProxyManagerImpl;
import dev.isxander.controlify.impl.config.settings.device.DeviceSettings;
import dev.isxander.controlify.impl.config.settings.profile.InputSettings;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.input.mapping.ControllerMapping;
import dev.isxander.controlify.impl.input.mapping.ControllerMappingManager;
import dev.isxander.controlify.impl.input.pipeline.InputPipeline;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class InputComponentImpl extends ControllerComponent implements InputComponent {
	public static final Identifier ID = CUtil.rl("input");

	private final Identifier controllerType;
	private DeviceSettings deviceSettings;
	private ControllerMapping controllerMapping;

	private final InputPipeline inputPipeline;
	private final ActionDispatcher actionDispatcher;

	private final Map<Identifier, ActionImpl> actions;

	private final Set<Identifier> physicalButtons, physicalAxes;
	private final boolean isGamepad;

	private EventSource.Subscription actionDispatcherSub;

	public InputComponentImpl(
			Identifier controllerType,
			InputPipeline inputPipeline,
			Set<Identifier> physicalButtons,
			Set<Identifier> physicalAxes,
			boolean isGamepad
	) {
		super(ID);
		this.controllerType = controllerType;
		this.inputPipeline = inputPipeline;

		this.physicalButtons = Set.copyOf(physicalButtons);
		this.physicalAxes = Set.copyOf(physicalAxes);
		this.isGamepad = isGamepad;

		this.actionDispatcher = new ActionDispatcher();
		this.actions = new LinkedHashMap<>();
	}

	@Override
	public void attach(ControllerImpl controller) {
		super.attach(controller);

		this.deviceSettings = Controlify.instance().config().getSettings()
				.getOrCreateDeviceSettings(controller.uid());

		this.reloadControllerMapping();
		this.inputPipeline.stageDeadzone().setDeadzoneSupplier(axis -> settings().sensitivity.getDeadzone((Identifier) axis));

		this.inputPipeline.stageInputDomainExpansion().setThresholdSupplier(() -> settings().buttonActivationThreshold);

		this.reloadActions();
		this.actionDispatcherSub = this.inputPipeline.sourceLogicalSignals().subscribe(signal -> {
			this.actionDispatcher.accept(signal);
			ActionProxyManagerImpl.INSTANCE.update(this.controller());
		});
	}

	@Override
	public @Nullable ActionImpl getAction(@NonNull Identifier actionId) {
		return this.actions.get(actionId);
	}

	public Collection<ActionImpl> getAllActions() {
		return Collections.unmodifiableCollection(this.actions.values());
	}

	public ActionDispatcher actionDispatcher() {
		return this.actionDispatcher;
	}

	public InputPipeline inputPipeline() {
		return this.inputPipeline;
	}

	public ControllerMapping controllerMapping() {
		return this.controllerMapping;
	}

	/// Refreshes resource-defined mappings while preserving a device-specific override.
	public void reloadControllerMapping() {
		ControllerMapping controllerMapping = this.deviceSettings.mapping;
		if (controllerMapping == null) {
			ControllerMappingManager controllerMappingManager = Controlify.instance().controllerMappingManager();
			controllerMapping = this.controller().info().type().mappingId()
					.flatMap(controllerMappingManager::getMappingById)
					.orElse(null);
		}
		if (controllerMapping == null) {
			controllerMapping = this.isGamepad
					? ControllerMapping.GAMEPAD_DEADZONES_ONLY_MAPPING
					: ControllerMapping.NO_MAPPING;
		}
		if (controllerMapping.equals(this.controllerMapping)) {
			return;
		}
		this.actionDispatcher.resetInputState();
		ActionProxyManagerImpl.INSTANCE.update(this.controller());
		this.controllerMapping = controllerMapping;
		this.inputPipeline.setControllerMapping(controllerMapping);
	}

	public void resetActions() {
		this.actionDispatcher.resetActions();
		ActionProxyManagerImpl.INSTANCE.update(this.controller());
	}

	public void reloadDefaultInputs() {
		DefaultBindProvider provider = Controlify.instance().defaultBindManager()
				.getDefaultBindProvider(this.controllerType);
		for (ActionImpl action : this.actions.values()) {
			action.setDefaultInput(provider.getDefaultInput(action.spec().id()));
			if (!settings().bindings.bindings.containsKey(action.spec().id())) {
				action.setBoundInput(action.defaultInput());
			}
		}
	}

	public void reloadActions() {
		ActionProxyManagerImpl.INSTANCE.release(this.controller());
		this.actions.clear();
		this.actionDispatcher.clearActions();

		for (ActionSpec spec : ActionSpecRegistryImpl.INSTANCE.getActionSpecs()) {
			ActionImpl action = this.createAction(spec);
			this.actions.put(spec.id(), action);
			this.actionDispatcher.addAction(action);
		}
	}

	public void onGuiNavigate() {
		this.inputPipeline.stagePhysicalToLogical().onGuiNavigate();
	}

	public InputSettings settings() {
		return this.controller().settings().input;
	}

	public InputSettings defaultSettings() {
		return this.controller().defaultSettings().input;
	}

	@Override
	public boolean isGamepad() {
		return this.isGamepad;
	}

	public Set<Identifier> physicalButtons() {
		return this.physicalButtons;
	}

	public Set<Identifier> physicalAxes() {
		return this.physicalAxes;
	}

	@Override
	public void close() {
		ActionProxyManagerImpl.INSTANCE.release(this.controller());
		super.close();

		this.actionDispatcherSub.close();
		this.actionDispatcherSub = null;
	}

	private ActionImpl createAction(ActionSpec spec) {
		DefaultBindProvider bindProvider = Controlify.instance().defaultBindManager()
				.getDefaultBindProvider(this.controllerType);
		LogicalInput defaultBoundInput = bindProvider.getDefaultInput(spec.id());

		LogicalInput currentBoundInput = settings().bindings.resolve(spec.id(), defaultBoundInput);

		ActivatorConfig<?> activatorConfig = Controlify.instance().activatorConfigManager()
				.getConfig(spec.id());

		ActionImpl action = new ActionImpl(
				spec,
				currentBoundInput,
				defaultBoundInput,
				activatorConfig,
				this.actionDispatcher
		);
		action.reset();

		return action;
	}
}
