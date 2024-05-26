package dev.isxander.controlify.api.ingameinput;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.platform.Event;
import org.joml.Vector2f;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Callbacks should modify the {@link #lookInput()} vector to modify the look input.
 * Be aware that multiple callbacks could be called, so make sure to be considerate of other mods.
 */
public record LookInputModifier(Vector2f lookInput, ControllerEntity controller) {
    /**
     * Creates a callback to modify the x and y individually.
     *
     * @param x the function for modifying the x-axis
     * @param y the function for modifying the y-axis
     * @return the new callback
     */
    public static Event.Callback<LookInputModifier> functional(BiFunction<Float, ControllerEntity, Float> x, BiFunction<Float, ControllerEntity, Float> y) {
        return event -> {
            event.lookInput.x = x.apply(event.lookInput.x, event.controller);
            event.lookInput.y = y.apply(event.lookInput.y, event.controller);
        };
    }

    /**
     * Creates a new callback that zeroes out the look input if the predicate is true.
     *
     * @param condition the condition that, if true, sets both axes to zero
     * @return the new LookInputModifier object
     */
    static Event.Callback<LookInputModifier> zeroIf(Predicate<ControllerEntity> condition) {
        return event -> {
            if (condition.test(event.controller)) {
                event.lookInput.set(0, 0);
            }
        };
    }
}
