package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A gesture that represents a tap on a specific input.
 * <p>
 * This gesture supports {@link ChannelKind#PULSE}.
 *
 * @see Signal.Tapped
 */
public record TapGesture(ResourceLocation input) implements Gesture {

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
}
