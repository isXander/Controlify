package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
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
public record TapGesture(ResourceLocation input) implements SerializableGesture<TapGesture> {

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

    public static final String GESTURE_ID = "tap";
    public static final MapCodec<TapGesture> MAP_CODEC =
            ResourceLocation.CODEC.fieldOf(GESTURE_ID).xmap(TapGesture::new, TapGesture::input);

    @Override
    public GestureType<TapGesture> type() {
        return GestureType.TAP;
    }
}
