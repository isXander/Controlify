package dev.isxander.controlify.input.input;

import dev.isxander.controlify.input.pipeline.*;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Converts IMU sensor data samples into accumulated tick-integrated deltas.
 * <ul>
 *     <li><strong>Gyro:</strong> rad/s -> dθ (rad)</li>
 *     <li><strong>Accelerometer:</strong> m/s² -> dv (m/s)</li>
 * </ul>
 */
public class SensorAccumulatorStage implements PacedEventStage<SensorEvent.Continuous, SensorEvent.Interval> {
    private final Map<SensorType, Acc> accSensorValues = new EnumMap<>(SensorType.class);

    @Override
    public void onEvent(SensorEvent.Continuous event, EventSink<? super SensorEvent.Interval> downstream) {
        SensorType sensorType = event.sensorType();
        long timeNanos = event.timestamp();
        float[] value = event.data();

        var acc = this.accSensorValues.computeIfAbsent(sensorType, st -> new Acc(value.length));
        // reallocate if sensor dimension changed (it shouldn't)
        if (acc.sum.length != value.length) {
            acc.sum = Arrays.copyOf(value, value.length);
        }

        // can't integrate from one sample, just store and wait for the next
        if (!acc.seen) {
            acc.lastNs = timeNanos;
            acc.seen = true;
            return;
        }

        long deltaNs = timeNanos - acc.lastNs;
        acc.lastNs = timeNanos;
        if (deltaNs <= 0) return; // guard clock anomalies

        float deltaS = deltaNs * 1e-9f; // ns -> s
        for (int i = 0; i < value.length; i++) {
            acc.sum[i] += value[i] * deltaS; // integrate: (per-second) * dt
        }
    }

    @Override
    public EventSubscription attachClock(Clock clock, EventSink<? super SensorEvent.Interval> downstream) {
        return clock.subscribe(tick -> {
            // emit integrated sensor deltas since the last tick, then reset accumulators
            this.accSensorValues.forEach((sensorType, acc) -> {
                if (acc.seen) {
                    downstream.accept(new SensorEvent.Interval(acc.lastNs, tick.deltaTimeMs(), sensorType, acc.sum.clone()));
                    Arrays.fill(acc.sum, 0f);
                    acc.lastNs = tick.deltaTimeMs();
                }
            });
        });
    }

    private static final class Acc {
        private long lastNs;  // last sample time
        private boolean seen; // have we initialised lastNs?
        private float[] sum;  // integrated sum (value * dt), length = sensor dimension

        private Acc(int dimension) {
            this.sum = new float[dimension];
        }
    }
}
