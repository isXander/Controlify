package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.impl.debug.DebugProperties;
import dev.isxander.controlify.impl.input.mapping.DeadzoneGroup;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.UnaryEventStage;
import dev.isxander.controlify.impl.utils.CUtil;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/// Applies radial deadzones to physical axes.
public class DeadzoneStage implements UnaryEventStage<PhysicalSignal> {

	private Map<Identifier, RadialDeadzoneState> stateByAxis;
	private Object2FloatFunction<Identifier> deadzoneByGroupName;

	public DeadzoneStage(Set<DeadzoneGroup> deadzoneGroups) {
		this.setDeadzoneGroups(deadzoneGroups);
		this.deadzoneByGroupName = _ -> 0f;
	}

	public DeadzoneStage() {
		this(Set.of());
	}

	@Override
	public void onEvent(@NonNull PhysicalSignal event, @NonNull EventSink<? super PhysicalSignal> downstream) {
		if (event instanceof PhysicalInputSignal.AxisMoved axisMoved) {
			RadialDeadzoneState state = this.stateByAxis.get(axisMoved.input());

			if (state != null) {
				state.onEvent(axisMoved, downstream);
				return;
			}
			if (DebugProperties.DEBUG_DEADZONES) {
				CUtil.LOGGER.log("[Deadzone] Unchanged (no group): {}", axisMoved);
			}
		}

		downstream.accept(event);
	}

	public void setDeadzoneGroups(Collection<DeadzoneGroup> deadzoneGroups) {
		this.stateByAxis = deadzoneGroups.stream()
				.map(RadialDeadzoneState::new)
				.flatMap(state -> state.deadzoneGroup.axes().stream().map(axis -> Map.entry(axis, state)))
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(_, _) -> {
							throw new IllegalArgumentException("Axis belongs to multiple deadzone groups");
						},
						Object2ObjectOpenHashMap::new
				));
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
		public void onEvent(PhysicalInputSignal.@NonNull AxisMoved event, @NonNull EventSink<? super PhysicalInputSignal.AxisMoved> downstream) {
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

			if (DebugProperties.DEBUG_DEADZONES) {
				CUtil.LOGGER.log("[Deadzone] Input: {}; group={}; threshold={}; rawAxes={}; magnitude={}; scale={}",
						event, this.deadzoneGroup.name(), deadzone, this.rawAxes, magnitude, scale);
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
