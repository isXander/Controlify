package dev.isxander.controlify.bindings.bind;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.joml.Vector2fc;

import java.util.Arrays;

public interface BindModifier {
    Vector2fc modifyVector(Vector2fc vector);

    float modifyAnalogue(float analogue);

    boolean modifyDigital(boolean digital);

    default BindModifier merge(BindModifier modifier) {
        return new BindModifier() {
            @Override
            public Vector2fc modifyVector(Vector2fc vector) {
                return modifier.modifyVector(BindModifier.this.modifyVector(vector));
            }

            @Override
            public float modifyAnalogue(float analogue) {
                return modifier.modifyAnalogue(BindModifier.this.modifyAnalogue(analogue));
            }

            @Override
            public boolean modifyDigital(boolean digital) {
                return modifier.modifyDigital(BindModifier.this.modifyDigital(digital));
            }
        };
    }

    static BindModifier mergeAll(BindModifier... modifiers) {
        return Arrays.stream(modifiers).reduce(BindModifier::merge).orElseThrow();
    }

    static BindModifier mergeAll(Iterable<BindModifier> modifiers) {
        return mergeAll(Iterables.toArray(modifiers, BindModifier.class));
    }
}
