package dev.isxander.controlify.input.action;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Set;

/**
 * Represents the specification of an action. This spec
 * is used to construct an Action for each pipeline.
 */
public record ActionSpec(
        ResourceLocation id,
        Component name,
        Component description,
        Component category,
        ChannelKind channelKind,
        Set<ResourceLocation> contexts,
        int priority
) implements Comparable<ActionSpec> {
    @Override
    public int compareTo(@NotNull ActionSpec other) {
        return Comparator
                .comparingInt(ActionSpec::priority)
                .thenComparing(ActionSpec::id)
                .compare(this, other);
    }
}
