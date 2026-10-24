/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.impl.input.mapping.ControllerMapping;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.utils.event.EventTap;
import org.jspecify.annotations.NonNull;

public class InputPipeline implements EventSink<PhysicalSignal> {
	private final EventTap<PhysicalSignal> tapRawPhysicalSignals;
	private final SensorProcessorStage stageSensorProcessor;
	private final MappingStage stageMapping;
	private final DeadzoneStage stageDeadzone;
	private final GestureRecogniserStage stageGestureRecogniser;
	private final InputDomainExpansionStage stageInputDomainExpansion;
	private final EventTap<PhysicalSignal> tapProcessedPhysicalSignals;

	private final SensorAccumulatorStage stageSensorAccumulator;
	private final PhysicalToLogicalStage stagePhysicalToLogical;
	private final EventTap<LogicalSignal> tapLogicalSignals;

	private final EventSink<PhysicalSignal> fullPipeline;

	public InputPipeline() {
		this.tapRawPhysicalSignals = new EventTap<>();
		this.stageSensorProcessor = new SensorProcessorStage();
		this.stageMapping = new MappingStage(ControllerMapping.NO_MAPPING);
		this.stageDeadzone = new DeadzoneStage();
		this.stageGestureRecogniser = new GestureRecogniserStage();
		this.stageInputDomainExpansion = new InputDomainExpansionStage(0.5f, 0.49f, 1f, 0f);
		this.tapProcessedPhysicalSignals = new EventTap<>();
		this.stageSensorAccumulator = new SensorAccumulatorStage();
		this.stagePhysicalToLogical = new PhysicalToLogicalStage(this.stageSensorAccumulator);
		this.tapLogicalSignals = new EventTap<>();

		this.fullPipeline = this.tapRawPhysicalSignals
				.andThen(this.stageSensorProcessor)
				.andThen(this.stageMapping)
				.andThen(this.stageDeadzone)
				.andThen(this.stageGestureRecogniser)
				.andThen(this.stageInputDomainExpansion)
				.andThen(this.tapProcessedPhysicalSignals)
				.andThen(this.stagePhysicalToLogical)
				.andThen(this.tapLogicalSignals)
				.discardDownstream();
	}

	/// Applies a new physical mapping without reopening the driver or replacing event sources.
	/// The caller must reset actions to release outputs based on the previous mapping.
	public void setControllerMapping(ControllerMapping mapping) {
		this.stageMapping.setControllerMapping(mapping);
		this.stageDeadzone.setDeadzoneGroups(mapping.deadzones().values());
		this.stageInputDomainExpansion.resetInputState();
		this.stagePhysicalToLogical.resetInputState();
	}

	@Override
	public void accept(@NonNull PhysicalSignal event) {
		this.fullPipeline.accept(event);
	}

	public EventSource<PhysicalSignal> sourceRawPhysicalSignals() {
		return this.tapRawPhysicalSignals;
	}

	public SensorProcessorStage stageSensorProcessor() {
		return this.stageSensorProcessor;
	}

	public MappingStage stageMapping() {
		return this.stageMapping;
	}

	public DeadzoneStage stageDeadzone() {
		return this.stageDeadzone;
	}

	public GestureRecogniserStage stageGestureRecogniser() {
		return this.stageGestureRecogniser;
	}

	public InputDomainExpansionStage stageInputDomainExpansion() {
		return this.stageInputDomainExpansion;
	}

	public EventSource<PhysicalSignal> sourceProcessedPhysicalSignals() {
		return this.tapProcessedPhysicalSignals;
	}

	public SensorAccumulatorStage stageSensorAccumulator() {
		return this.stageSensorAccumulator;
	}

	public PhysicalToLogicalStage stagePhysicalToLogical() {
		return this.stagePhysicalToLogical;
	}

	public EventSource<LogicalSignal> sourceLogicalSignals() {
		return this.tapLogicalSignals;
	}
}
