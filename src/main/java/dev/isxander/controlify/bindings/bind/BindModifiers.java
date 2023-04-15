package dev.isxander.controlify.bindings.bind;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.function.UnaryOperator;

public final class BindModifiers {
    public static final BindModifier IDENTITY = new FunctionalModifier(
            UnaryOperator.identity(),
            UnaryOperator.identity(),
            UnaryOperator.identity()
    );

    /**
     * Negates vectors and floats, and inverts booleans.
     */
    public static BindModifier INVERT = new FunctionalModifier(
            v -> new Vector2f(v).negate(),
            f -> -f,
            b -> !b
    );

    public static BindModifier INVERT_X = new FunctionalModifier(
            v -> new Vector2f(-v.x(), v.y()),
            f -> -f,
            b -> !b
    );

    public static BindModifier INVERT_Y = new FunctionalModifier(
            v -> new Vector2f(v.x(), -v.y()),
            f -> -f,
            b -> !b
    );

    /**
     * Rounds vectors and floats to 0 or 1.
     */
    public static BindModifier ANALOGUE_TO_DIGITAL = new FunctionalModifier(
            v -> new Vector2f(
                    Math.abs(v.x()) > 0.5f ? Math.copySign(1f, v.x()) : 0,
                    Math.abs(v.y()) > 0.5f ? Math.copySign(1f, v.y()) : 0
            ),
            f -> f > 0.5f ? 1f : 0f,
            b -> b
    );

    /**
     * Remaps the range of vectors and floats from [-1, 1] to [0, 1].
     */
    public static BindModifier REMAP_POSITIVE = new FunctionalModifier(
            v -> new Vector2f(
                    (1 + v.x()) / 2,
                    (1 + v.y()) / 2
            ),
            f -> (1 + f) / 2,
            b -> b
    );

    /**
     * Removes Y component from vectors
     */
    public static BindModifier ONLY_X = onlyVec(
            v -> new Vector2f(v.x(), 0)
    );

    /**
     * Removes X component from vectors
     */
    public static BindModifier ONLY_Y = onlyVec(
            v -> new Vector2f(0, v.y())
    );

    /**
     * Swaps the X and Y components of vectors
     */
    public static BindModifier SWAP_X_Y = onlyVec(
            v -> new Vector2f(v.y(), v.x())
    );

    private static FunctionalModifier onlyVec(UnaryOperator<Vector2fc> vecMod) {
        return new FunctionalModifier(
                vecMod,
                UnaryOperator.identity(),
                UnaryOperator.identity()
        );
    }

    private static FunctionalModifier onlyAnalogue(UnaryOperator<Float> analogueMod) {
        return new FunctionalModifier(
                UnaryOperator.identity(),
                analogueMod,
                UnaryOperator.identity()
        );
    }

    private static FunctionalModifier onlyDigital(UnaryOperator<Boolean> digitalMod) {
        return new FunctionalModifier(
                UnaryOperator.identity(),
                UnaryOperator.identity(),
                digitalMod
        );
    }

    private record FunctionalModifier(
            UnaryOperator<Vector2fc> vecMod,
            UnaryOperator<Float> analogueMod,
            UnaryOperator<Boolean> digitalMod
    ) implements BindModifier {
        @Override
        public Vector2fc modifyVector(Vector2fc vector) {
            return vecMod.apply(vector);
        }

        @Override
        public float modifyAnalogue(float analogue) {
            return analogueMod.apply(analogue);
        }

        @Override
        public boolean modifyDigital(boolean digital) {
            return digitalMod.apply(digital);
        }
    }
}
