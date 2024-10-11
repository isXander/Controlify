package dev.isxander.controlify.controller.gyro;

import org.joml.Vector3fc;

/**
 * Represents the current gyro delta in radians per second.
 */
public interface GyroStateC extends Vector3fc {
    GyroStateC ZERO = new GyroState(0, 0, 0);

    default float pitch() {
        return x();
    }

    default float yaw() {
        return y();
    }

    default float roll() {
        return z();
    }
}
