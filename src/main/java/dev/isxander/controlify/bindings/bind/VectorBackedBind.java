package dev.isxander.controlify.bindings.bind;

import org.joml.Vector2fc;

/**
 * A bind value backed by a vector.
 * <p>
 * The analogue value is the length of the vector and
 * the digital value is true if the analogue value is greater than the button actuation setting.
 *
 * @param vector
 */
public record VectorBackedBind(Vector2fc vector) implements BindValue {
    @Override
    public float analogue() {
        return vector.length();
    }

    @Override
    public boolean digital() {
        return analogue() > 0.5f;
    }

    @Override
    public boolean isBackedByType(BindType type) {
        return type == BindType.VECTOR;
    }

    @Override
    public BindValue modify(BindModifier modifier) {
        return new VectorBackedBind(modifier.modifyVector(vector));
    }
}
