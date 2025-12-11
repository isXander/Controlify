package dev.isxander.controlify.bindings.defaults;

import dev.isxander.controlify.bindings.input.Input;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public interface DefaultBindProvider {
    @Nullable
    Input getDefaultBind(Identifier bindId);

    DefaultBindProvider EMPTY = bind -> null;
}
