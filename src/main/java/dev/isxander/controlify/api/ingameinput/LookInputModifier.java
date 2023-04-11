package dev.isxander.controlify.api.ingameinput;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.ingame.InGameInputHandler;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

public interface LookInputModifier {
    float modifyX(float x, Controller<?, ?> controller);

    float modifyY(float y, Controller<?, ?> controller);

    static LookInputModifier functional(BiFunction<Float, Controller<?, ?>, Float> x, BiFunction<Float, Controller<?, ?>, Float> y) {
        return new InGameInputHandler.FunctionalLookInputModifier(x, y);
    }

    static LookInputModifier zeroIf(BooleanSupplier condition) {
        return functional((x, controller) -> condition.getAsBoolean() ? 0 : x, (y, controller) -> condition.getAsBoolean() ? 0 : y);
    }
}
