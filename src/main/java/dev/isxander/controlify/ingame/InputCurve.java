package dev.isxander.controlify.ingame;

/**
 * A functional interface representing an input curve transformation.
 * Outputs must retain the same sign as inputs, so that negative inputs produce negative outputs.
 */
@FunctionalInterface
public interface InputCurve {
    double apply(double input);

    static InputCurve power(double exponent) {
        return d -> Math.signum(d) * Math.pow(Math.abs(d), exponent);
    }

    static InputCurve cubicBlend(double blendFactor) {
        return d -> {
            double mag = Math.abs(d);
            double blended = (1 - blendFactor) * mag + blendFactor * mag * mag * mag;
            return Math.signum(d) * blended;
        };
    }

    static InputCurve sCurve() {
        return d -> {
            double mag = Math.abs(d);
            double sCurved = mag * mag * (3 - 2 * mag);
            return Math.signum(d) * sCurved;
        };
    }
}
