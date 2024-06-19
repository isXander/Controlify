package dev.isxander.controlify.bindings;

import java.util.function.BooleanSupplier;

public interface KeyMappingHandle {
    void controlify$setPressed(boolean isDown);

    void controlify$addToggleCondition(BooleanSupplier condition);
}
