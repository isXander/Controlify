/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.impl.input.SensorType;
import dev.isxander.controlify.impl.input.signal.PhysicalSensorSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.UnaryEventStage;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class SensorProcessorStage implements UnaryEventStage<PhysicalSignal> {
	private final Map<SensorType, List<SensorProcessor>> processors;

	public SensorProcessorStage() {
		this(_ -> List.of());
	}

	public SensorProcessorStage(Function<SensorType, List<SensorProcessor>> processorFactory) {
		this.processors = new EnumMap<>(SensorType.class);
		this.setProcessors(processorFactory);
	}

	public void setProcessors(Function<SensorType, List<SensorProcessor>> processorFactory) {
		this.processors.clear();
		for (SensorType sensorType : SensorType.values()) {
			this.processors.put(sensorType, List.copyOf(processorFactory.apply(sensorType)));
		}
	}

	/// Configures one sensor without replacing processing owned by other components.
	public void setProcessors(SensorType sensorType, List<SensorProcessor> processors) {
		this.processors.put(sensorType, List.copyOf(processors));
	}

	@Override
	public void onEvent(@NonNull PhysicalSignal event, @NonNull EventSink<? super PhysicalSignal> downstream) {
		switch (event) {
			case PhysicalSensorSignal(long timeNanos, long sensorTimeNanos, SensorType sensorType, float x, float y, float z)
					when !this.processors.get(sensorType).isEmpty() -> {
				var data = new Vector3f(x, y, z);

				for (SensorProcessor processor : this.processors.get(sensorType)) {
					// Filtering and integration share the host event clock, including frame boundaries.
					processor.process(data, timeNanos);
				}

				downstream.accept(new PhysicalSensorSignal(timeNanos, sensorTimeNanos, sensorType, data.x, data.y, data.z));
			}
			default -> downstream.accept(event);
		}
	}

	public interface SensorProcessor {
		void process(Vector3f data, long timeNanos);
	}

	public record BiasCorrection(Supplier<Vector3fc> bias) implements SensorProcessor {
		@Override
		public void process(Vector3f data, long timeNanos) {
			data.sub(this.bias.get());
		}
	}
}
