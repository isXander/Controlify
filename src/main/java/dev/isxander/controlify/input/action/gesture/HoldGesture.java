package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A gesture that represents a hold action on a specific input.
 * <p>
 * This gesture supports both {@link ChannelKind#LATCH} and {@link ChannelKind#PULSE}.
 * <ul>
 *     <li>The latch state is set to true when the button is pressed down and false when it is released.</li>
 *     <li>A pulse is fired when the button is held down for a certain duration.</li>
 * </ul>
 * @see Signal.ButtonDown
 * @see Signal.ButtonUp
 * @see Signal.Held
 */
public record HoldGesture(ResourceLocation input) implements SerializableGesture<HoldGesture> {

    @Override
    public boolean supports(ChannelKind channel) {
        return channel.isLatch() || channel.isPulse();
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {
        switch (signal) {
            case Signal.ButtonDown s -> {
                if (s.input().equals(this.input)) {
                    acc.setLatch(true);
                }
            }
            case Signal.ButtonUp s -> {
                if (s.input().equals(this.input)) {
                    acc.setLatch(false);
                }
            }
            case Signal.Held s -> {
                if (s.input().equals(this.input)) {
                    acc.firePulse();
                }
            }

            default -> {}
        }
    }

    @Override
    public String describe() {
        return "Hold[" + input.toString() + "]";
    }

    @Override
    public Set<ResourceLocation> monitoredInputs() {
        return Set.of(input);
    }

    public static final String GESTURE_ID = "hold";
    public static final MapCodec<HoldGesture> MAP_CODEC =
            ResourceLocation.CODEC.fieldOf(GESTURE_ID).xmap(HoldGesture::new, HoldGesture::input);

    @Override
    public GestureType<HoldGesture> type() {
        return GestureType.HOLD;
    }
}

