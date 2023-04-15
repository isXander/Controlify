package dev.isxander.controlify.api.ingameinput;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.ingame.InGameInputHandler;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

/**
 * Allows dependants to modify controller look input.
 *
 * Implementing classes must provide methods for modifying the x
 * and y axis values of the controller's look input.
 */
public interface LookInputModifier {

    /**
     * Modifies the x axis value of the controller's look input.
     *
     * @param x the current value of the x axis, typically in the range 0-1 but can be higher from gyro input
     * @param controller the current active controller
     * @return the modified value of the x axis
     */
    float modifyX(float x, Controller<?, ?> controller);

    /**
     * Modifies the y axis value of the controller's look input.
     *
     * @param y the current value of the y axis, typically in the range 0-1 but can be higher from gyro input
     * @param controller the current active controller
     * @return the modified value of the y axis
     */
    float modifyY(float y, Controller<?, ?> controller);

    /**
     * Creates a new LookInputModifier using the given x and y axis modifying functions.
     *
     * @param x the function for modifying the x axis
     * @param y the function for modifying the y axis
     * @return the new LookInputModifier object
     */
    static LookInputModifier functional(BiFunction<Float, Controller<?, ?>, Float> x, BiFunction<Float, Controller<?, ?>, Float> y) {
        return new InGameInputHandler.FunctionalLookInputModifier(x, y);
    }

    /**
     * Creates a new LookInputModifier that sets the x and y axis to zero if the given condition is true.
     *
     * @param condition the condition that determines whether to set the axis values to zero
     * @return the new LookInputModifier object
     */
    static LookInputModifier zeroIf(BooleanSupplier condition) {
        return functional((x, controller) -> condition.getAsBoolean() ? 0 : x, (y, controller) -> condition.getAsBoolean() ? 0 : y);
    }
}
