package dev.isxander.controlify.input.action;

import net.minecraft.resources.ResourceLocation;

public record ActionId<T extends Channel>(ResourceLocation id) {
    public static ActionId<Channel.Continuous> continuous(ResourceLocation id) {
        return new ActionId<>(id);
    }
    public static ActionId<Channel.Pulse> pulse(ResourceLocation id) {
        return new ActionId<>(id);
    }
    public static ActionId<Channel.Latch> latch(ResourceLocation id) {
        return new ActionId<>(id);
    }
}
