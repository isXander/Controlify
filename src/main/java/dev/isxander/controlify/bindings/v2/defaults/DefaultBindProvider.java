package dev.isxander.controlify.bindings.v2.defaults;

import dev.isxander.controlify.bindings.Bind;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface DefaultBindProvider {
    @Nullable
    Bind getDefaultBind(ResourceLocation bindId);

    DefaultBindProvider EMPTY = bind -> null;
}
