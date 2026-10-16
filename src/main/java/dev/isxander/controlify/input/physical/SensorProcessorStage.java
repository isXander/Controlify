package dev.isxander.controlify.input.physical;

import dev.isxander.controlify.input.SensorType;
import dev.isxander.controlify.utils.event.EventSink;
import dev.isxander.controlify.utils.event.UnaryEventStage;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
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

	@Override
	public void onEvent(PhysicalSignal event, EventSink<? super PhysicalSignal> downstream) {
		switch (event) {
			case PhysicalSensorSignal(long timeNanos, long sensorTimeNanos, SensorType sensorType, float x, float y, float z)
					when !this.processors.get(sensorType).isEmpty() -> {
				var data = new Vector3f(x, y, z);

				for (SensorProcessor processor : this.processors.get(sensorType)) {
					processor.process(data, sensorTimeNanos);
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

	/// A first-order low-pass filter with a finite, positive cutoff frequency in Hz.
	/// Lower frequencies smooth more strongly. The first sample passes through unchanged.
	/// History expires after a gap of 50ms or when the sensor clock moves backwards.
	///
	/// If the frequency is set to 0, the filter is bypassed.
	public static class LowPassFilter implements SensorProcessor {
		private static final long MAX_SAMPLE_AGE_NANOS = 50_000_000L;
		private final DoubleSupplier cutoffHz;
		private @Nullable Vector3f previous;
		private long previousTimeNanos;

		public LowPassFilter(DoubleSupplier cutoffHz) {
			this.cutoffHz = cutoffHz;
		}

		@Override
		public void process(Vector3f data, long timeNanos) {
			double cutoffHz = this.cutoffHz.getAsDouble();

			long elapsedNanos = timeNanos - this.previousTimeNanos;
			if (cutoffHz == 0 || this.previous == null
					|| elapsedNanos > MAX_SAMPLE_AGE_NANOS || elapsedNanos < 0) {
				this.previous = new Vector3f(data);
				this.previousTimeNanos = timeNanos;
			} else {
				if (elapsedNanos > 0) {
					// Exponential decay keeps the response consistent across sample rates.
					// expm1 preserves precision for very short sample intervals.
					double decayPerNano = 2.0 * Math.PI * cutoffHz * 1e-9;
					float blend = (float) -Math.expm1(-elapsedNanos * decayPerNano);
					this.previous.lerp(data, blend);
					this.previousTimeNanos = timeNanos;
				}
				data.set(this.previous);
			}
		}
	}
}
