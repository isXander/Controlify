package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.action.gesture.builder.DoubleTapGestureBuilder;
import dev.isxander.controlify.input.action.gesture.builder.GestureBuilder;
import dev.isxander.controlify.input.input.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;

/**
 * A gesture that represents a double tap on a specific input.
 *
 * @see Signal.DoubleTapped
 */
public record DoubleTapGesture(ResourceLocation input) implements Gesture {
    @Override
    public boolean supports(ChannelKind channel) {
        return channel.isPulse();
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {
        if (signal instanceof Signal.DoubleTapped(long ignored, ResourceLocation input) && this.input.equals(input)) {
            acc.firePulse();
        }
    }

    @Override
    public String describe() {
        return "DoubleTap[" + input.toString() + "]";
    }

    @Override
    public Set<ResourceLocation> monitoredInputs() {
        return Set.of(input);
    }

    @Override
    public GestureBuilder<?, ?> toBuilder() {
        return new DoubleTapGestureBuilder(
                Optional.of(this.input())
        );
    }
}
