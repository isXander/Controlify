package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.action.gesture.builder.GestureBuilder;
import dev.isxander.controlify.input.action.gesture.builder.ToggleGestureBuilder;
import dev.isxander.controlify.input.input.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;

/**
 * A gesture that toggles a latch state based on a pulse gesture.
 * When a pulse is received, it toggles the latch state of the target accumulator.
 * <p>
 * This gesture supports {@link ChannelKind#LATCH}.
 */
public final class ToggleGesture implements GestureModifier {
    private final Gesture pulseGesture;
    private final PulseToToggleAcc pulseToToggleAcc;

    public ToggleGesture(Gesture pulseGesture) {
        if (!pulseGesture.supports(ChannelKind.PULSE)) {
            throw new IllegalArgumentException("Pulse gesture is required for ToggleGesture");
        }
        this.pulseGesture = pulseGesture;
        this.pulseToToggleAcc = new PulseToToggleAcc();
    }

    @Override
    public boolean supports(ChannelKind channel) {
        return channel.isLatch();
    }

    @Override
    public ChannelKind inputChannelKind() {
        return ChannelKind.PULSE;
    }

    @Override
    public Gesture baseGesture() {
        return this.pulseGesture;
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {
        this.pulseToToggleAcc.target = acc;
        this.pulseGesture.onSignal(signal, this.pulseToToggleAcc);
    }

    @Override
    public String describe() {
        return "Toggle[" + this.pulseGesture.describe() + "]";
    }

    @Override
    public GestureBuilder<?, ?> toBuilder() {
        return new ToggleGestureBuilder(
                Optional.of(this.baseGesture().toBuilder())
        );
    }

    public static ToggleGesture ofTap(ResourceLocation input) {
        return new ToggleGesture(new TapGesture(input));
    }

    public static ToggleGesture ofHold(ResourceLocation input) {
        return new ToggleGesture(new HoldGesture(input));
    }

    private static final class PulseToToggleAcc implements Accumulator {
        private Accumulator target;

        @Override
        public void firePulse() {
            target.toggleLatch();
        }
    }
}


