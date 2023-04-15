package dev.isxander.controlify.bindings.bind;

import org.joml.Vector2f;
import org.joml.Vector2fc;

public record FloatBackedBind(float analogue) implements BindValue {
    @Override
    public Vector2fc vector() {
        return new Vector2f(analogue, analogue);
    }

    @Override
    public boolean digital() {
        return analogue > 0.5f;
    }

    @Override
    public boolean isBackedByType(BindType type) {
        return type == BindType.ANALOGUE;
    }

    @Override
    public BindValue modify(BindModifier modifier) {
        return new FloatBackedBind(modifier.modifyAnalogue(analogue));
    }
}
