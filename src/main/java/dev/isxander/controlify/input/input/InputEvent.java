package dev.isxander.controlify.input.input;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents the first layer of the input pipeline in Controlify.
 * Input events are the raw events that come from a controller.
 */
public sealed interface InputEvent {
    /**
     * The time of the event in nanoseconds.
     */
    long timestamp();

    /**
     * The input identifier, represented as a ResourceLocation.
     * @return the input identifier
     */
    ResourceLocation input();

    /**
     * Represents a button press/release event.
     * @param timestamp the time of the event in nanoseconds.
     * @param input the input identifier, represented as a ResourceLocation.
     * @param state the state of the button, where true indicates pressed and false indicates released.
     * @see dev.isxander.controlify.controller.input.GamepadInputs
     * @see dev.isxander.controlify.controller.input.JoystickInputs
     */
    record ButtonChanged(long timestamp, ResourceLocation input, boolean state) implements InputEvent {}

    /**
     * Represents an axis movement event.
     * @param timestamp the time of the event in nanoseconds.
     * @param input the input identifier, represented as a ResourceLocation.
     * @param value the value of the axis, *always* in the range [0.0, 1.0].
     * @see dev.isxander.controlify.controller.input.GamepadInputs
     * @see dev.isxander.controlify.controller.input.JoystickInputs
     */
    record AxisMoved(long timestamp, ResourceLocation input, float value) implements InputEvent {}

}
