package dev.isxander.controlify.input.physical;

import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.UnaryEventStage;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/// Applies deadzones to physical axes.
// TODO: radial deadzones
public class DeadzoneStage implements UnaryEventStage<PhysicalSignal> {
	private Function<Identifier, Float> deadzoneSupplier;

	private final Set<Identifier> axisWasZero;

	public DeadzoneStage(Function<Identifier, Float> deadzoneSupplier) {
		this.deadzoneSupplier = deadzoneSupplier;
		this.axisWasZero = new HashSet<>();
	}

	public DeadzoneStage() {
		this(_ -> 0f);
	}

	@Override
	public void onEvent(PhysicalSignal event, EventSink<? super PhysicalSignal> downstream) {
		if (event instanceof PhysicalInputSignal.AxisMoved(long timeNanos, Identifier input, float value)) {
			float deadzone = this.deadzoneSupplier.apply(input);

			if (deadzone > 0) {
				float newValue = ControllerUtils.deadzone(value, deadzone);

				if (newValue != 0 || this.axisWasZero.add(input)) {
					downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, input, newValue));
					if (newValue != 0) {
						this.axisWasZero.remove(input);
					}
				}
				return;
			} else {
				this.axisWasZero.remove(input);
			}
		}

		downstream.accept(event);
	}

	public void setDeadzoneSupplier(Function<Identifier, Float> deadzoneSupplier) {
		this.deadzoneSupplier = deadzoneSupplier;
	}
}
