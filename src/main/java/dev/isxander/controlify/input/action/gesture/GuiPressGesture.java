package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A gesture that represents a GUI press signal.
 * <p>
 * This gesture supports {@link ChannelKind#PULSE}
 *
 * @see Signal.GuiPress
 */
public record GuiPressGesture(ResourceLocation input) implements SerializableGesture<GuiPressGesture> {

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

    public static final String GESTURE_ID = "gui_press";
    public static final MapCodec<GuiPressGesture> MAP_CODEC =
            ResourceLocation.CODEC.fieldOf(GESTURE_ID).xmap(GuiPressGesture::new, GuiPressGesture::input);

    @Override
    public GestureType<GuiPressGesture> type() {
        return GestureType.GUI_PRESS;
    }
}

