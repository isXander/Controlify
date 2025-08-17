package dev.isxander.controlify.input.input;

import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.UnaryEventStage;
import dev.isxander.controlify.utils.ControllerUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * An event stage in the input pipeline that applies a deadzone to axis movements.
 * The deadzone is determined by a supplier function that takes the axis as input.
 */
public class DeadzoneStage implements UnaryEventStage<InputEvent> {
    private final Function<ResourceLocation, Float> deadzoneSupplier;

    public DeadzoneStage(Function<ResourceLocation, Float> deadzoneSupplier) {
        this.deadzoneSupplier = deadzoneSupplier;
    }

    @Override
    public void onEvent(InputEvent event, EventSink<? super InputEvent> downstream) {
        if (event instanceof InputEvent.AxisMoved(long timeNanos, ResourceLocation axis, float value)) {
            float deadzone = deadzoneSupplier.apply(axis);

            if (deadzone > 0) {
                float newValue = ControllerUtils.deadzone(value, deadzone);

                downstream.accept(
                        new InputEvent.AxisMoved(timeNanos, axis, newValue)
                );
                return;
            }
        }

        downstream.accept(event);
    }
}
