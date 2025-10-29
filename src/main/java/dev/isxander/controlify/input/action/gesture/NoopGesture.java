package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.action.gesture.builder.GestureBuilder;
import dev.isxander.controlify.input.action.gesture.builder.NoopGestureBuilder;
import dev.isxander.controlify.input.input.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A no-op gesture that does nothing when signaled.
 * This is used for unbound actions.
 */
public class NoopGesture implements Gesture {
    public static final NoopGesture INSTANCE = new NoopGesture();

    @Override
    public boolean supports(ChannelKind channel) {
        return true;
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {

    }

    @Override
    public String describe() {
        return "No-op";
    }

    @Override
    public Set<ResourceLocation> monitoredInputs() {
        return Set.of();
    }

    @Override
    public GestureBuilder<?, ?> toBuilder() {
        return NoopGestureBuilder.INSTANCE;
    }
}

