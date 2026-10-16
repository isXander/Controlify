package dev.isxander.controlify.input.physical;

import dev.isxander.controlify.controller.input.DeadzoneGroup;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.UnaryEventStage;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/// Applies radial deadzones to physical axes.
public class DeadzoneStage implements UnaryEventStage<PhysicalSignal> {

	private final Map<Identifier, DeadzoneGroup> groupByAxis;
	private final Map<DeadzoneGroup, RadialDeadzoneState> stateByGroup;

	private Object2FloatFunction<Identifier> deadzoneByGroupName;

	public DeadzoneStage(Set<DeadzoneGroup> deadzoneGroups) {
		this.groupByAxis = deadzoneGroups.stream()
				.flatMap(g -> g.axes().stream().map(a -> Map.entry(a, g)))
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(_, _) -> {
							throw new IllegalArgumentException("Axis belongs to multiple deadzone groups");
						},
						Object2ObjectOpenHashMap::new
				));
		this.stateByGroup = new Object2ObjectArrayMap<>();
		this.deadzoneByGroupName = _ -> 0f;
	}

	@Override
	public void onEvent(PhysicalSignal event, EventSink<? super PhysicalSignal> downstream) {
		if (event instanceof PhysicalInputSignal.AxisMoved axisMoved) {
			DeadzoneGroup deadzoneGroup = this.groupByAxis.get(axisMoved.input());

			if (deadzoneGroup != null) {
				RadialDeadzoneState state = this.stateByGroup.computeIfAbsent(deadzoneGroup, RadialDeadzoneState::new);
				state.onEvent(axisMoved, downstream);
				return;
			}
		}

		downstream.accept(event);
	}

	public void setDeadzoneSupplier(Object2FloatFunction<Identifier> deadzoneByGroupName) {
		this.deadzoneByGroupName = deadzoneByGroupName;
	}

	private class RadialDeadzoneState implements UnaryEventStage<PhysicalInputSignal.AxisMoved> {
		private final DeadzoneGroup deadzoneGroup;
		private final Object2FloatMap<Identifier> rawAxes = new Object2FloatArrayMap<>(4);
		private final Object2FloatMap<Identifier> outputAxes = new Object2FloatArrayMap<>(4);

		public RadialDeadzoneState(DeadzoneGroup deadzoneGroup) {
			this.deadzoneGroup = deadzoneGroup;

			for (Identifier axis : deadzoneGroup.axes()) {
				this.rawAxes.put(axis, 0.0f);
				this.outputAxes.put(axis, 0.0f);
			}
		}

		@Override
		public void onEvent(PhysicalInputSignal.AxisMoved event, EventSink<? super PhysicalInputSignal.AxisMoved> downstream) {
			this.rawAxes.put(event.input(), event.value());

			float deadzone = DeadzoneStage.this.deadzoneByGroupName.getFloat(this.deadzoneGroup.name());

			float magnitudeSqr = 0.0f;
			for (float value : this.rawAxes.values()) {
				magnitudeSqr += value * value;
			}
			float magnitude = Mth.sqrt(magnitudeSqr);

			float scale;
			if (magnitude <= deadzone) {
				scale = 0.0f;
			} else {
				float outputMagnitude = Math.min(
						(magnitude - deadzone) / (1.0f - deadzone),
						1.0f
				);
				scale = outputMagnitude / magnitude;
			}

			for (var entry : Object2FloatMaps.fastIterable(this.rawAxes)) {
				Identifier axis = entry.getKey();

				float newValue = entry.getFloatValue() * scale;
				float oldValue = this.outputAxes.put(axis, newValue);

				if (newValue != oldValue) {
					downstream.accept(new PhysicalInputSignal.AxisMoved(event.timeNanos(), axis, newValue));
				}
			}
		}
	}
}
