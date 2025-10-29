package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.action.gesture.builder.GestureBuilder;
import dev.isxander.controlify.input.input.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface Gesture {
    Codec<Gesture> CODEC = GestureBuilder.CODEC.xmap(GestureBuilder::build, Gesture::toBuilder);

    /**
     * Checks if this gesture supports the given channel kind.
     * @param channel the channel kind to check
     * @return true if this gesture supports the channel kind, false otherwise
     */
    boolean supports(ChannelKind channel);

    /**
     * Handles an incoming signal and updates the given accumulator accordingly.
     * @param signal the incoming signal
     * @param acc the accumulator to update
     */
    void onSignal(Signal signal, Accumulator acc);

    /**
     * Provides a human-readable description of this gesture.
     * @return a string describing this gesture
     */
    String describe();

    /**
     * Returns a set of input resource locations that this gesture monitors.
     * @implNote {@link #onSignal(Signal, Accumulator)} is only called for signals involving these inputs.
     * @return a set of monitored input resource locations
     */
    Set<ResourceLocation> monitoredInputs();

    /**
     * Converts this gesture into a builder for serialization purposes.
     * Implementations should provide a builder that is able to be built without
     * throwing {@link dev.isxander.controlify.input.action.gesture.builder.IncompleteBuildException}
     * @return a gesture builder representing this gesture
     */
    GestureBuilder<?, ?> toBuilder();

    /**
     * Checks if this gesture is the same as another gesture.
     * @return true if this gesture is the same as the obj argument; false otherwise.
     */
    default boolean isSame(Gesture gesture) {
        return this.toBuilder().equals(gesture.toBuilder());
    }
}
