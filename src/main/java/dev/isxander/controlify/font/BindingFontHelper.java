package dev.isxander.controlify.font;

import dev.isxander.controlify.api.bind.ControllerBinding;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class BindingFontHelper {
    public static Component binding(ResourceLocation binding) {
        return MutableComponent.create(new BindComponentContents(binding));
    }

    public static Component binding(ControllerBinding binding) {
        return binding(binding.id());
    }
}
