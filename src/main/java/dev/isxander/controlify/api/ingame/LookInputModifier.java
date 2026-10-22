package dev.isxander.controlify.api.ingame;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.utils.event.EventSink;
import org.joml.Vector2f;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/// Modify lookInput in place before application to the player. Callbacks compose in
/// subscription order. The vector belongs to this dispatch and must not be retained.
public record LookInputModifier(Vector2f lookInput, Controller controller) {
	public static EventSink<LookInputModifier> functional(
			BiFunction<Float, Controller, Float> x, BiFunction<Float, Controller, Float> y) {
		return event -> {
			event.lookInput.x = x.apply(event.lookInput.x, event.controller);
			event.lookInput.y = y.apply(event.lookInput.y, event.controller);
		};
	}

	public static EventSink<LookInputModifier> zeroIf(Predicate<Controller> condition) {
		return event -> {
			if (condition.test(event.controller)) {
				event.lookInput.set(0, 0);
			}
		};
	}
}
