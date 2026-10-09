package dev.isxander.controlify.input.logical;

import dev.isxander.controlify.input.physical.PhysicalSensorSignal;
import dev.isxander.controlify.input.physical.PhysicalSignal;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.EventStage;

/// The event stage that converts physical signals into logical ones.
/// This stage is responsible for recognizing chords, integrating sensor data, and emitting logical signals.
public class PhysicalToLogicalStage implements EventStage<PhysicalSignal, LogicalSignal> {
	private final SensorAccumulatorStage sensorAccumulatorStage;

	public PhysicalToLogicalStage(SensorAccumulatorStage sensorAccumulatorStage) {
		this.sensorAccumulatorStage = sensorAccumulatorStage;
	}

	@Override
	public void onEvent(PhysicalSignal event, EventSink<? super LogicalSignal> downstream) {
		switch (event) {
			case PhysicalSignal.Tick(long timeNanos, long deltaTimeNanos) -> {
				this.sensorAccumulatorStage.onEvent(event, downstream);

				downstream.accept(new LogicalSignal.Tick(timeNanos, deltaTimeNanos));
			}

			case PhysicalSensorSignal sensorSignal ->
					this.sensorAccumulatorStage.onEvent(sensorSignal, downstream);
		}
	}
}
