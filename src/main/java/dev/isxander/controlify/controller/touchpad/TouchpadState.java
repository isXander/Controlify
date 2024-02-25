package dev.isxander.controlify.controller.touchpad;

import org.joml.Vector2fc;

import java.util.List;

public record TouchpadState(List<Finger> fingers, int maxFingers) {
    public static TouchpadState empty(int maxFingers) {
        return new TouchpadState(List.of(), maxFingers);
    }

    public record Finger(Vector2fc position, float pressure) {
    }
}
