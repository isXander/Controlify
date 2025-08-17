package dev.isxander.controlify.input.action;

import net.minecraft.resources.ResourceLocation;

public record Context(ResourceLocation id, int priority, ConsumePolicy consumePolicy) {
}
