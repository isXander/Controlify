package dev.isxander.controlify.input.logical;

import dev.isxander.controlify.input.SensorType;
import dev.isxander.controlify.input.physical.PhysicalSensorSignal;
import dev.isxander.controlify.input.physical.PhysicalSignal;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.EventStage;

import java.util.EnumMap;
import java.util.Map;

public class SensorAccumulatorStage implements EventStage<PhysicalSignal, LogicalSignal.SensorInterval> {
	// prevent stale readings from being continually accumulated
	private static final long MAX_SAMPLE_AGE_NANOS = 50_000_000L;

	private final Map<SensorType, Acc> accSensorValues = new EnumMap<>(SensorType.class);

	@Override
	public void onEvent(
			PhysicalSignal event,
			EventSink<? super LogicalSignal.SensorInterval> downstream
	) {
		switch (event) {
			case PhysicalSignal.Tick(long timeNanos, _) -> {
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

			case PhysicalSensorSignal(long timeNanos, SensorType sensorType, float x, float y, float z) -> {
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

			// Integrate only while the last sensor reading is considered valid.
			long validUntilNanos = lastSampleNanos + MAX_SAMPLE_AGE_NANOS;
			long integrationEndNanos = Math.min(timeNanos, validUntilNanos);

			if (integrationEndNanos > lastNs) {
				long integrationNanos = integrationEndNanos - lastNs;
				float deltaSeconds = integrationNanos * 1e-9f;

				x += velocityX * deltaSeconds;
				y += velocityY * deltaSeconds;
				z += velocityZ * deltaSeconds;

				accumulatedNanos += integrationNanos;
			}

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
