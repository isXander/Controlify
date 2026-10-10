package dev.isxander.controlify.input.physical;

import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.UnaryEventStage;

/// Processes sensor data and recognizes distinct gestures such as shaking of the controller.
/// This stage then emits {@link dev.isxander.controlify.input.physical.PhysicalInputSignal.ButtonChanged} events
/// for when the gesture begins and ends using input identifiers from {@link GestureInputs}.
public class GestureRecogniserStage implements UnaryEventStage<PhysicalSignal> {
	@Override
	public void onEvent(PhysicalSignal event, EventSink<? super PhysicalSignal> downstream) {
		// TODO
		downstream.accept(event);
	}
}
