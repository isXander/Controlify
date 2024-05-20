package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.input.Input;
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

    InputBindingBuilder defaultInput(@Nullable Input input);

    InputBindingBuilder allowedContexts(@NotNull BindContext @Nullable... contexts);

    InputBindingBuilder radialCandidate(@Nullable ResourceLocation icon);
}
