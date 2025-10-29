package dev.isxander.controlify.input.action;

import net.minecraft.resources.ResourceLocation;

public record Context(ResourceLocation id, int priority, ConsumePolicy consumePolicy) {
    public static final int DEFAULT_PRIORITY = 0;
    public static final ConsumePolicy DEFAULT_CONSUME_POLICY = ConsumePolicy.CONSUME;

    public static Context withDefaults(ResourceLocation id) {
        return new Context(id, DEFAULT_PRIORITY, DEFAULT_CONSUME_POLICY);
    }

    public static Context withPriority(ResourceLocation id, int priority) {
        return new Context(id, priority, DEFAULT_CONSUME_POLICY);
    }

    public static Context withConsumePolicy(ResourceLocation id, ConsumePolicy consumePolicy) {
        return new Context(id, DEFAULT_PRIORITY, consumePolicy);
    }
}
