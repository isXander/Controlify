package dev.isxander.controlify.controller;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class Inputs {
    private Inputs() {
    }

    public static ResourceLocation getThemedSprite(ResourceLocation input, String theme) {
        return new ResourceLocation(input.getNamespace(), "inputs/" + theme + "/" + input.getPath());
    }

    public static MutableComponent getInputComponent(ResourceLocation input) {
        return Component.translatable("controlify.input." + input.getNamespace() + "." + input.getPath());
    }
}
