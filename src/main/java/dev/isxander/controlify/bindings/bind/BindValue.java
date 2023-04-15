package dev.isxander.controlify.bindings.bind;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.Collection;

/**
 * A value for a bind that can be expressed in multiple ways.
 */
public interface BindValue {
    /**
     * @return the vector representation of the bind value.
     */
    Vector2fc vector();

    /**
     * @return the analogue representation of the bind value.
     */
    float analogue();

    /**
     * @return the digital representation of the bind value.
     */
    boolean digital();

    BindValue modify(BindModifier modifier);

    default BindValue modify(Collection<BindModifier> modifiers) {
        return modify(BindModifier.mergeAll(modifiers));
    }

    boolean isBackedByType(BindType type);

    static BindValue of(Vector2fc vector) {
        return new VectorBackedBind(vector);
    }

    static BindValue of(float x, float y) {
        return new VectorBackedBind(new Vector2f(x, y));
    }

    static BindValue of(boolean xPos, boolean xNeg, boolean yPos, boolean yNeg) {
        return new VectorBackedBind(new Vector2f(
                (xPos ? 1 : 0) - (xNeg ? 1 : 0),
                (yPos ? 1 : 0) - (yNeg ? 1 : 0)
        ));
    }

    static BindValue of(float analogue) {
        return new FloatBackedBind(analogue);
    }

    static BindValue of(boolean digital) {
        return new BooleanBackedBind(digital);
    }

}
