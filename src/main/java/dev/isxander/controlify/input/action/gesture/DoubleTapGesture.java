package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A gesture that represents a double tap on a specific input.
 *
 * @see Signal.DoubleTapped
 */
public record DoubleTapGesture(ResourceLocation input) implements SerializableGesture<DoubleTapGesture> {
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

    public static final String GESTURE_ID = "double_tap";
    public static final MapCodec<DoubleTapGesture> MAP_CODEC =
            ResourceLocation.CODEC.fieldOf(GESTURE_ID).xmap(DoubleTapGesture::new, DoubleTapGesture::input);

    @Override
    public GestureType<DoubleTapGesture> type() {
        return GestureType.DOUBLE_TAP;
    }
}
