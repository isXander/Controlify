package dev.isxander.controlify.utils.animation.api;

import net.minecraft.util.Mth;

/**
 * Represents a function through 0-1.
 *
 * @see <a href="https://easings.net">https://easings.net</a>
 */
public interface EasingFunction {
    float ease(float t);

    EasingFunction LINEAR = t -> t;

    EasingFunction EASE_IN_SIN = t -> 1 - Mth.cos(t * Mth.PI / 2);
    EasingFunction EASE_OUT_SIN = t -> Mth.sin(t * Mth.PI / 2);
    EasingFunction EASE_IN_OUT_SIN = t -> -(Mth.cos(Mth.PI * t) - 1) / 2;

    EasingFunction EASE_IN_QUAD = t -> t * t;
    EasingFunction EASE_OUT_QUAD = t -> 1 - (1 - t) * (1 - t);
    EasingFunction EASE_IN_OUT_QUAD = t -> t < 0.5 ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;

    EasingFunction EASE_IN_CUBIC = t -> t * t * t;
    EasingFunction EASE_OUT_CUBIC = t -> 1 - (1 - t) * (1 - t) * (1 - t);
    EasingFunction EASE_IN_OUT_CUBIC = t -> t < 0.5 ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;

    EasingFunction EASE_IN_QUART = t -> t * t * t * t;
    EasingFunction EASE_OUT_QUART = t -> 1 - (1 - t) * (1 - t) * (1 - t) * (1 - t);
    EasingFunction EASE_IN_OUT_QUART = t -> t < 0.5 ? 8 * t * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 4) / 2;

    EasingFunction EASE_IN_QUINT = t -> t * t * t * t * t;
    EasingFunction EASE_OUT_QUINT = t -> 1 - (1 - t) * (1 - t) * (1 - t) * (1 - t) * (1 - t);
    EasingFunction EASE_IN_OUT_QUINT = t -> t < 0.5 ? 16 * t * t * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 5) / 2;

    EasingFunction EASE_IN_EXPO = t -> t == 0 ? 0 : (float) Math.pow(2, 10 * t - 10);
    EasingFunction EASE_OUT_EXPO = t -> t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
    EasingFunction EASE_IN_OUT_EXPO = t -> t == 0 ? 0 : t == 1 ? 1 : t < 0.5 ? (float) Math.pow(2, 20 * t - 10) / 2 : (2 - (float) Math.pow(2, -20 * t + 10)) / 2;

    EasingFunction EASE_IN_CIRC = t -> 1 - (float) Math.sqrt(1 - Math.pow(t, 2));
    EasingFunction EASE_OUT_CIRC = t -> (float) Math.sqrt(1 - Math.pow(t - 1, 2));
    EasingFunction EASE_IN_OUT_CIRC = t -> t < 0.5 ? (1 - (float) Math.sqrt(1 - Math.pow(2 * t, 2))) / 2 : ((float) Math.sqrt(1 - Math.pow(-2 * t + 2, 2)) + 1) / 2;

    EasingFunction EASE_IN_BACK = t -> (float) (t * t * (2.70158 * t - 1.70158));
    EasingFunction EASE_OUT_BACK = t -> 1 - (float) ((1 - t) * (1 - t) * (-2.70158 * (1 - t) - 1.70158));
    EasingFunction EASE_IN_OUT_BACK = t -> t < 0.5 ? (float) (2 * t * t * (3.5949095 * t - 2.5949095)) : 1 - (float) ((2 - 2 * t) * (2 - 2 * t) * (3.5949095 * (2 - 2 * t) - 2.5949095)) / 2;

    EasingFunction EASE_IN_ELASTIC = t -> t == 0 ? 0 : t == 1 ? 1 : -(float) Math.pow(2, 10 * t - 10) * (float) Math.sin((t * 10 - 10.75) * (2 * Math.PI) / 3);
    EasingFunction EASE_OUT_ELASTIC = t -> t == 0 ? 0 : t == 1 ? 1 : (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75) * (2 * Math.PI) / 3) + 1;
    EasingFunction EASE_IN_OUT_ELASTIC = t -> t == 0 ? 0 : t == 1 ? 1 : t < 0.5 ? -(float) Math.pow(2, 20 * t - 10) * (float) Math.sin((20 * t - 11.125) * (2 * Math.PI) / 4.5) / 2 : (float) Math.pow(2, -20 * t + 10) * (float) Math.sin((20 * t - 11.125) * (2 * Math.PI) / 4.5) / 2 + 1;

    EasingFunction EASE_OUT_BOUNCE = t -> {
        if (t < 1 / 2.75) {
            return 7.5625f * t * t;
        } else if (t < 2 / 2.75) {
            return 7.5625f * (t -= 1.5f / 2.75f) * t + 0.75f;
        } else if (t < 2.5 / 2.75) {
            return 7.5625f * (t -= 2.25f / 2.75f) * t + 0.9375f;
        } else {
            return 7.5625f * (t -= 2.625f / 2.75f) * t + 0.984375f;
        }
    };
    EasingFunction EASE_IN_BOUNCE = t -> 1 - EASE_OUT_BOUNCE.ease(1 - t);
    EasingFunction EASE_IN_OUT_BOUNCE = t -> t < 0.5 ? (1 - EASE_OUT_BOUNCE.ease(1 - 2 * t)) / 2 : (1 + EASE_OUT_BOUNCE.ease(2 * t - 1)) / 2;
}
