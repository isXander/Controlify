package dev.isxander.controlify.api.ingameinput;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.platform.Event;
import org.joml.Vector2f;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

/**
 * Allows dependants to modify controller look input.
 *
 * Implementing classes must provide methods for modifying the x
 * and y axis values of the controller's look input.
 */
public record LookInputModifier(Vector2f lookInput, ControllerEntity controller) {
    /**
     * Creates a new LookInputModifier using the given x and y axis modifying functions.
     *
     * @param x the function for modifying the x axis
     * @param y the function for modifying the y axis
     * @return the new LookInputModifier object
     */
    static Event.Callback<LookInputModifier> functional(BiFunction<Float, ControllerEntity, Float> x, BiFunction<Float, ControllerEntity, Float> y) {
        return new InGameInputHandler.FunctionalLookInputModifier(x, y);
    }

    /**
     * Creates a new LookInputModifier that sets the x and y axis to zero if the given condition is true.
     *
     * @param condition the condition that determines whether to set the axis values to zero
     * @return the new LookInputModifier object
     */
    static Event.Callback<LookInputModifier> zeroIf(BooleanSupplier condition) {
        return functional((x, controller) -> condition.getAsBoolean() ? 0 : x, (y, controller) -> condition.getAsBoolean() ? 0 : y);
    }
}
