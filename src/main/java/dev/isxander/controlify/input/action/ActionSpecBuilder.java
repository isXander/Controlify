package dev.isxander.controlify.input.action;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface ActionSpecBuilder<H extends ActionHandle> {
    ActionSpecBuilder<H> id(ResourceLocation id);

    ActionSpecBuilder<H> name(Component name);
    ActionSpecBuilder<H> description(Component description);
    ActionSpecBuilder<H> category(Component category);

    ActionSpecBuilder<H> context(ResourceLocation... contexts);

    ActionSpecBuilder<H> priority(int priority);

    ActionAccessor<H> buildAndRegister(ActionSpecRegistry registry);

    static ActionSpecBuilder<ActionHandle.Latch> latch() {
        return new ActionSpecBuilderImpl<>(ActionHandle.Latch::new, ChannelKind.LATCH);
    }
    static ActionSpecBuilder<ActionHandle.Pulse> pulse() {
        return new ActionSpecBuilderImpl<>(ActionHandle.Pulse::new, ChannelKind.PULSE);
    }
    static ActionSpecBuilder<ActionHandle.Continuous> continuous() {
        return new ActionSpecBuilderImpl<>(ActionHandle.Continuous::new, ChannelKind.CONTINUOUS);
    }
}
