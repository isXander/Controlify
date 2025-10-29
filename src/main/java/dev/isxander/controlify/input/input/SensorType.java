package dev.isxander.controlify.input.input;

public enum SensorType {
    /**
     * Sensor components are <code>xyz</code> and in radians.
     */
    GYROSCOPE,
    /**
     * Sensor components are <code>xyz</code> and in radians.
     */
    GYROSCOPE_L,
    /**
     * Sensor components are <code>xyz</code> and in radians.
     */
    GYROSCOPE_R,
    /**
     * Sensor components are <code>xyz</code> and in m/s^2.
     */
    ACCELEROMETER,
    /**
     * Sensor components are <code>xyz</code> and in m/s^2.
     */
    ACCELEROMETER_L,
    /**
     * Sensor components are <code>xyz</code> and in m/s^2.
     */
    ACCELEROMETER_R;

    public boolean isGyroscope() {
        return this == GYROSCOPE || this == GYROSCOPE_L || this == GYROSCOPE_R;
    }
    public boolean isAccelerometer() {
        return this == ACCELEROMETER || this == ACCELEROMETER_L || this == ACCELEROMETER_R;
    }
}
