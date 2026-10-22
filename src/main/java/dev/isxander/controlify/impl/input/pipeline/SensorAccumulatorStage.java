/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.pipeline;

import dev.isxander.controlify.impl.input.SensorType;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSensorSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.EventStage;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;

public class SensorAccumulatorStage implements EventStage<PhysicalSignal, LogicalSignal.SensorInterval> {
	// prevent stale readings from being continually accumulated
	private static final long MAX_SAMPLE_AGE_NANOS = 50_000_000L;

	private final Map<SensorType, Acc> accSensorValues = new EnumMap<>(SensorType.class);

	@Override
	public void onEvent(
			@NonNull PhysicalSignal event,
			@NonNull EventSink<? super LogicalSignal.SensorInterval> downstream
	) {
		switch (event) {
			case PhysicalSignal.Frame(long timeNanos, _) -> {
				this.accSensorValues.values().removeIf(acc ->
						acc.lastSampleNanos != -1 && timeNanos - acc.lastSampleNanos > MAX_SAMPLE_AGE_NANOS);
				this.accSensorValues.forEach((sensorType, acc) -> {
					if (acc.lastNs == -1) {
						return;
					}

					acc.integrateUntil(timeNanos);

					if (acc.accumulatedNanos > 0) {
						float x = acc.x;
						float y = acc.y;
						float z = acc.z;

						if (sensorType.isAccelerometer()) {
							float durationSeconds = acc.accumulatedNanos * 1e-9f;

							x /= durationSeconds;
							y /= durationSeconds;
							z /= durationSeconds;
						}

						downstream.accept(new LogicalSignal.SensorInterval(
								timeNanos,
								acc.accumulatedNanos,
								sensorType,
								x,
								y,
								z
						));
					}

					acc.resetAccumulation();
				});
			}

			case PhysicalSensorSignal(long timeNanos, _, SensorType sensorType, float x, float y, float z) -> {
				Acc acc = this.accSensorValues.computeIfAbsent(sensorType, _ -> new Acc());

				if (acc.lastNs != -1 && timeNanos <= acc.lastNs) {
					return;
				}

				acc.integrateUntil(timeNanos);

				acc.velocityX = x;
				acc.velocityY = y;
				acc.velocityZ = z;
				acc.lastSampleNanos = timeNanos;
			}

			default -> {
			}
		}
	}

	private static final class Acc {
		private long lastNs = -1;
		private long lastSampleNanos = -1;

		private float velocityX;
		private float velocityY;
		private float velocityZ;

		private float x;
		private float y;
		private float z;

		private long accumulatedNanos;

		public void integrateUntil(long timeNanos) {
			if (lastNs == -1) {
				lastNs = timeNanos;
				return;
			}

			long deltaNanos = timeNanos - lastNs;

			if (deltaNanos <= 0) {
				return;
			}

			if (lastSampleNanos == -1 || timeNanos - lastSampleNanos > MAX_SAMPLE_AGE_NANOS) {
				// A gap invalidates both the held sample and any motion waiting for a frame.
				resetAccumulation();
				lastSampleNanos = -1;
				lastNs = timeNanos;
				return;
			}

			float deltaSeconds = deltaNanos * 1e-9f;
			x += velocityX * deltaSeconds;
			y += velocityY * deltaSeconds;
			z += velocityZ * deltaSeconds;
			accumulatedNanos += deltaNanos;
			lastNs = timeNanos;
		}

		public void resetAccumulation() {
			x = 0.0f;
			y = 0.0f;
			z = 0.0f;
			accumulatedNanos = 0;
		}
	}
}
