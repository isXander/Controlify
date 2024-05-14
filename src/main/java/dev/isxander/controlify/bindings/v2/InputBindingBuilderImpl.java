package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.v2.inputmask.Bind;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InputBindingBuilderImpl implements InputBindingBuilder {
    private final ControllerEntity controller;

    private @Nullable ResourceLocation id;
    private @Nullable Component customName, customDescription;
    private @Nullable Bind defaultBind;
    private final Set<BindContext> allowedContexts = new HashSet<>();
    private @Nullable RadialIcon radialCandidate;

    public InputBindingBuilderImpl(ControllerEntity controller) {
        this.controller = controller;
    }

    @Override
    public InputBindingBuilder id(@NotNull ResourceLocation rl) {
        this.id = rl;
        return this;
    }

    @Override
    public InputBindingBuilder id(@NotNull String namespace, @NotNull String path) {
        return this.id(new ResourceLocation(namespace, path));
    }

    @Override
    public InputBindingBuilder name(@NotNull Component text) {
        this.customName = text;
        return this;
    }

    @Override
    public InputBindingBuilder description(@NotNull Component text) {
        this.customDescription = text;
        return this;
    }

    @Override
    public InputBindingBuilder defaultBind(@Nullable Bind bind) {
        this.defaultBind = bind;
        return this;
    }

    @Override
    public InputBindingBuilder allowedContexts(@NotNull BindContext @Nullable ... contexts) {
        if (contexts != null)
            this.allowedContexts.addAll(List.of(contexts));
        return this;
    }

    @Override
    public InputBindingBuilder radialCandidate(@Nullable RadialIcon icon) {
        this.radialCandidate = icon;
        return this;
    }

    public InputBinding build() {
        new InputBindingImpl(controller, id, customName, customDescription, defaultBind, () -> defaultBind, allowedContexts, radialCandidate);
    }
}
