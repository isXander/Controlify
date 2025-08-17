package dev.isxander.controlify.input.input;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents the first layer of the input pipeline in Controlify.
 * Input events are the raw events that come from a controller.
 */
public sealed interface InputEvent {
    long timeNanos();

    /** Called once per frame */
    record Tick(long timeNanos) implements InputEvent {}

    record ButtonPress(long timeNanos, ResourceLocation input) implements InputEvent {}
    record ButtonRelease(long timeNanos, ResourceLocation input) implements InputEvent {}

    record AxisMoved(long timeNanos, ResourceLocation input, float value) implements InputEvent {}
}
