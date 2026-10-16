package dev.isxander.controlify.input;

import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import dev.isxander.controlify.input.logical.LogicalSignal;
import dev.isxander.controlify.input.logical.PhysicalToLogicalStage;
import dev.isxander.controlify.input.logical.SensorAccumulatorStage;
import dev.isxander.controlify.input.physical.*;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.EventSource;
import dev.isxander.controlify.utils.event.EventTap;

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

	@Override
	public void accept(PhysicalSignal event) {
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
