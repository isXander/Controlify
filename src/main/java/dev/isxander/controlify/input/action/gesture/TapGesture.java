package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.action.gesture.builder.GestureBuilder;
import dev.isxander.controlify.input.action.gesture.builder.TapGestureBuilder;
import dev.isxander.controlify.input.input.Signal;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * A gesture that represents a tap on a specific input.
 * <p>
 * This gesture supports {@link ChannelKind#PULSE}.
 *
 * @see Signal.Tapped
 */
public record TapGesture(@NotNull ResourceLocation input) implements Gesture {

    @Override
    public boolean supports(ChannelKind channel) {
        return channel.isPulse();
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {
        if (signal instanceof Signal.Tapped(long ignored, ResourceLocation input) && input.equals(this.input)) {
            acc.firePulse();
        }
    }

    @Override
    public String describe() {
        return "Tap[" + input.toString() + "]";
    }

    @Override
    public Set<ResourceLocation> monitoredInputs() {
        return Set.of(input);
    }

    @Override
    public GestureBuilder<?, ?> toBuilder() {
        return new TapGestureBuilder(
                Optional.of(this.input())
        );
    }
}
