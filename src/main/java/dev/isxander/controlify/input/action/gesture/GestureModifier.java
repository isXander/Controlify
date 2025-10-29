package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.ChannelKind;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A gesture that modifies the output of a base gesture in some way, such as turning a pulse into a latch.
 */
public interface GestureModifier extends Gesture {
    /**
     * @return the type of channel the base gesture produces.
     */
    ChannelKind inputChannelKind();

    /**
     * @return the base gesture that this modifier modifies.
     */
    Gesture baseGesture();

    @Override
    default Set<ResourceLocation> monitoredInputs() {
        return baseGesture().monitoredInputs();
    }
}
