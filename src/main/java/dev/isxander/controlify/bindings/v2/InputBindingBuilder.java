package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.v2.inputmask.Bind;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InputBindingBuilder {
    InputBindingBuilder id(@NotNull ResourceLocation rl);

    InputBindingBuilder id(@NotNull String namespace, @NotNull String path);

    InputBindingBuilder name(@NotNull Component text);

    InputBindingBuilder description(@NotNull Component text);

    InputBindingBuilder category(@NotNull Component text);

    InputBindingBuilder defaultBind(@Nullable Bind bind);

    InputBindingBuilder allowedContexts(@NotNull BindContext @Nullable... contexts);

    InputBindingBuilder radialCandidate(@Nullable ResourceLocation icon);
}
