package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.impl.input.mapping.GestureInputs;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.UnaryEventStage;
import org.jspecify.annotations.NonNull;

/// Processes sensor data and recognizes distinct gestures such as shaking of the controller.
/// This stage then emits {@link PhysicalInputSignal.ButtonChanged} events
/// for when the gesture begins and ends using input identifiers from {@link GestureInputs}.
public class GestureRecogniserStage implements UnaryEventStage<PhysicalSignal> {
	@Override
	public void onEvent(@NonNull PhysicalSignal event, @NonNull EventSink<? super PhysicalSignal> downstream) {
		// TODO
		downstream.accept(event);
	}
}
