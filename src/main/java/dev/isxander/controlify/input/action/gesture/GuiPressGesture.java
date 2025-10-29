package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.action.gesture.builder.GestureBuilder;
import dev.isxander.controlify.input.action.gesture.builder.GuiPressGestureBuilder;
import dev.isxander.controlify.input.input.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;

/**
 * A gesture that represents a GUI press signal.
 * <p>
 * This gesture supports {@link ChannelKind#PULSE}
 *
 * @see Signal.GuiPress
 */
public record GuiPressGesture(ResourceLocation input) implements Gesture {

    @Override
    public boolean supports(ChannelKind channel) {
        return channel.isPulse();
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {
        if (signal instanceof Signal.GuiPress s && s.input().equals(this.input)) {
            acc.firePulse();
        }
    }

    @Override
    public String describe() {
        return "GuiPress[" + input.toString() + "]";
    }

    @Override
    public Set<ResourceLocation> monitoredInputs() {
        return Set.of(input);
    }

    @Override
    public GestureBuilder<?, ?> toBuilder() {
        return new GuiPressGestureBuilder(
                Optional.of(this.input())
        );
    }
}

