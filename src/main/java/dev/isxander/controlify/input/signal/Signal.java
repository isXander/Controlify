package dev.isxander.controlify.input.signal;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents an input signal, derived from {@link dev.isxander.controlify.input.input.InputEvent}.
 */
public sealed interface Signal {
    long timeNanos();

    /** Submitted once per frame */
    record Tick(long timeNanos) implements Signal {}

    sealed interface InputSignal extends Signal {
        ResourceLocation input();
    }
    /** When button is pressed down */
    record ButtonDown(long timeNanos, ResourceLocation input) implements InputSignal {}
    /** When button is released */
    record ButtonUp(long timeNanos, ResourceLocation input) implements InputSignal {}
    /** When button is pressed and released in quick succession */
    record Tapped(long timeNanos, ResourceLocation input) implements InputSignal {}
    /** When two {@link Tapped taps} occur in quick succession */
    record DoubleTapped(long timeNanos, ResourceLocation input) implements InputSignal {}
    /** When a button is pressed and released, where no GUI navigation happened in between */
    record GuiPress(long timeNanos, ResourceLocation input) implements InputSignal {}
    /** When a button is held for a long period of time. Signaled whilst still held. */
    record Held(long timeNanos, ResourceLocation input) implements InputSignal {}
    /** When an axis value changes */
    record AxisMoved(long timeNanos, ResourceLocation input, float value, float delta) implements InputSignal {}
}
