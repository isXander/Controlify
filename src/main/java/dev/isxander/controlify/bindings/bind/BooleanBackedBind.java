package dev.isxander.controlify.bindings.bind;

import org.joml.Vector2f;
import org.joml.Vector2fc;

public record BooleanBackedBind(boolean digital) implements BindValue {
    @Override
    public Vector2fc vector() {
        return new Vector2f(analogue(), analogue());
    }

    @Override
    public float analogue() {
        return digital ? 1 : 0;
    }

    @Override
    public boolean isBackedByType(BindType type) {
        return type == BindType.DIGITAL;
    }

    @Override
    public BindValue modify(BindModifier modifier) {
        return new BooleanBackedBind(modifier.modifyDigital(digital));
    }
}
