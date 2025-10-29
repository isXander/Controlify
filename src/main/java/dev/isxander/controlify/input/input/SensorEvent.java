package dev.isxander.controlify.input.input;

public sealed interface SensorEvent {

    /**
     * @return timestamp of the event, in nanoseconds.
     */
    long timestamp();

    /**
     * @return type of sensor that produced this event.
     */
    SensorType sensorType();

    /**
     * The unit and dimensionality of the data is determined by the {@link SensorType}.
     * The unit space is defined by the implementation of this interface.
     * @return data from the sensor
     */
    float[] data();

    /**
     * A raw sensor sample.
     * @param data a sample of data from the sensor. For example, gyro is rad/s.
     */
    record Continuous(long timestamp, SensorType sensorType, float[] data) implements SensorEvent {}

    /**
     * An accumulated delta over a frame.
     * @param data the integrated delta over the frame. For example, gyro is in radians.
     */
    record Interval(long timestamp, long deltaTime, SensorType sensorType, float[] data) implements SensorEvent {}
}
