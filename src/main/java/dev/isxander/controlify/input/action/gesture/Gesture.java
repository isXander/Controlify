package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface Gesture {
    boolean supports(ChannelKind channel);

    void onSignal(Signal signal, Accumulator acc);

    String describe();

    Set<ResourceLocation> monitoredInputs();
}
