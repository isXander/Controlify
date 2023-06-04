package dev.isxander.controlify.utils;

public class Easings {
    public static float easeInQuad(float t) {
        return t * t;
    }

    public static float easeOutQuad(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    public static float easeOutExpo(float t) {
        return t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
    }
}
