package dev.isxander.controlify.controller.composable.gyro;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public class GyroState extends Vector3f implements GyroStateC {
    public GyroState(float pitch, float yaw, float roll) {
        super(pitch, yaw, roll);
    }

    public GyroState(GyroStateC vec) {
        super(vec);
    }

    public GyroState() {
    }

    @Override
    public GyroState mul(Vector3fc v) {
        super.mul(v);
        return this;
    }

    @Override
    public GyroState mul(float scalar) {
        super.mul(scalar);
        return this;
    }

    @Override
    public GyroState div(Vector3fc v) {
        super.div(v);
        return this;
    }

    @Override
    public GyroState div(float scalar) {
        super.div(scalar);
        return this;
    }

    @Override
    public GyroState sub(Vector3fc v) {
        super.sub(v);
        return this;
    }

    @Override
    public GyroState sub(float x, float y, float z) {
        super.sub(x, y, z);
        return this;
    }
}
