package dev.isxander.controlify.bindings.defaults;

import dev.isxander.controlify.bindings.input.Input;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface DefaultBindProvider {
    @Nullable
    Input getDefaultBind(ResourceLocation bindId);

    DefaultBindProvider EMPTY = bind -> null;
}
