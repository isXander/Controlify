package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A no-op gesture that does nothing when signaled.
 * This is used for unbound actions.
 */
public class NoopGesture implements SerializableGesture<NoopGesture> {
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

    public static final String GESTURE_ID = "noop";
    public static final MapCodec<NoopGesture> MAP_CODEC =
            Codec.unit(INSTANCE).fieldOf(GESTURE_ID);

    @Override
    public GestureType<NoopGesture> type() {
        return GestureType.NOOP;
    }
}

