package dev.isxander.controlify.input.physical;

import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.UnaryEventStage;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

/// Expands the domain of physical inputs.
/// - Expands button presses and releases into axis moved events.
/// - Expands axis moved events into button presses and releases when passing threshold.
public class InputDomainExpansionStage implements UnaryEventStage<PhysicalInputSignal> {

	private final Set<Identifier> pressedButtons;
	private final float actuation, release, pressedValue, releasedValue;

	public InputDomainExpansionStage(float actuation, float release, float pressedValue, float releasedValue) {
		this.pressedButtons = new HashSet<>();
		this.actuation = actuation;
		this.release = release;
		this.pressedValue = pressedValue;
		this.releasedValue = releasedValue;
	}

	@Override
	public void onEvent(PhysicalInputSignal event, EventSink<? super PhysicalInputSignal> downstream) {
		downstream.accept(event);

		switch (event) {
			case PhysicalInputSignal.AxisMoved(long timeNanos, Identifier input, float value) -> {
				if (value >= actuation) {
					if (!this.pressedButtons.add(input)) {
						downstream.accept(new PhysicalInputSignal.ButtonChanged(timeNanos, input, true));
					}
				} else if (value <= release) {
					if (!this.pressedButtons.remove(input)) {
						downstream.accept(new PhysicalInputSignal.ButtonChanged(timeNanos, input, false));
					}
				}
			}
			case PhysicalInputSignal.ButtonChanged(long timeNanos, Identifier input, boolean state) -> {
				downstream.accept(new PhysicalInputSignal.AxisMoved(timeNanos, input, state ? pressedValue : releasedValue));
			}
		}
	}
}
