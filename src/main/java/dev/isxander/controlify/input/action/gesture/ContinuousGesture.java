package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A gesture that represents a continuous input, such as an axis movement.
 * <p>
 * This gesture supports {@link ChannelKind#CONTINUOUS}.
 */
public record ContinuousGesture(ResourceLocation input) implements Gesture {
    @Override
    public boolean supports(ChannelKind channel) {
        return channel.isContinuous();
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {
        if (signal instanceof Signal.AxisMoved s && s.input().equals(this.input)) {
            acc.setAxis(s.value());
        }
    }

    @Override
    public String describe() {
        return "Continuous[" + input.toString() + "]";
    }

    @Override
    public Set<ResourceLocation> monitoredInputs() {
        return Set.of(input);
    }
}
