package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.action.gesture.Gesture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record CompiledBinding(
        ActionId<?> id,
        Gesture gesture,
        ChannelKind channelKind,
        Set<ResourceLocation> contexts,
        int priority
) implements Comparable<CompiledBinding> {

    public CompiledBinding {
        if (!gesture.supports(channelKind)) {
            throw new IllegalArgumentException("Gesture " + gesture.describe() + " does not support channel kind " + channelKind);
        }
    }

    @Override
    public int compareTo(@NotNull CompiledBinding o) {
        return Integer.compare(o.priority, this.priority);
    }
}
