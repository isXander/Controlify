package dev.isxander.controlify.bindings;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBindingBuilder;
import dev.isxander.controlify.bindings.defaults.DefaultBindProvider;
import dev.isxander.controlify.bindings.input.EmptyInput;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class InputBindingBuilderImpl implements InputBindingBuilder {
    private @Nullable ResourceLocation id;
    private @Nullable Component category;
    private @Nullable Component customName, customDescription;
    private @Nullable Input defaultInput;
    private final Set<BindContext> allowedContexts = new HashSet<>();
    private @Nullable ResourceLocation radialCandidate;

    private boolean locked;

    @Override
    public InputBindingBuilder id(@NotNull ResourceLocation rl) {
        checkLocked();

        this.id = rl;
        return this;
    }

    @Override
    public InputBindingBuilder id(@NotNull String namespace, @NotNull String path) {
        return this.id(new ResourceLocation(namespace, path));
    }

    @Override
    public InputBindingBuilder category(@NotNull Component text) {
        checkLocked();

        this.category = text;
        return this;
    }

    @Override
    public InputBindingBuilder name(@NotNull Component text) {
        checkLocked();

        this.customName = text;
        return this;
    }

    @Override
    public InputBindingBuilder description(@NotNull Component text) {
        checkLocked();

        this.customDescription = text;
        return this;
    }

    @Override
    public InputBindingBuilder defaultInput(@Nullable Input input) {
        checkLocked();

        this.defaultInput = input;
        return this;
    }

    @Override
    public InputBindingBuilder allowedContexts(@NotNull BindContext @Nullable ... contexts) {
        checkLocked();

        if (contexts != null)
            this.allowedContexts.addAll(List.of(contexts));
        return this;
    }

    @Override
    public InputBindingBuilder radialCandidate(@Nullable ResourceLocation icon) {
        checkLocked();

        this.radialCandidate = icon;
        return this;
    }

    public InputBindingImpl build(ControllerEntity controller) {
        Validate.isTrue(locked, "Tried to build builder before it was locked.");

        Component name = createDefaultString(null, false);
        if (customName != null) name = customName;

        Component description = createDefaultString("desc", true);
        if (customDescription != null) description = customDescription;
        if (description == null) description = Component.empty();

        Supplier<Input> defaultSupplier = () -> {
            // retrieve every tick so the bind provider isn't cached after a resource reload
            DefaultBindProvider provider = Controlify.instance().defaultBindManager().getDefaultBindProvider(
                    controller.info().type().namespace()
            );

            Input input = provider.getDefaultBind(id);
            if (input == null) input = defaultInput;
            if (input == null) input = EmptyInput.INSTANCE;

            return input;
        };

        return new InputBindingImpl(controller, id, name, description, category, defaultSupplier, allowedContexts, radialCandidate);
    }

    @NotNull
    public ResourceLocation getIdAndLock() {
        checkLocked();

        locked = true;
        Validate.notNull(this.id, "Must call `.id(ResourceLocation)` on builder!");
        Validate.notNull(this.category, "Must call `.category(Component)` on builder %s!".formatted(this.id));

        return this.id;
    }

    private void checkLocked() {
        Validate.isTrue(!locked, "Tried to modify binding builder after is has been locked!");
    }

    private Component createDefaultString(@Nullable String suffix, boolean notExistToNull) {
        Objects.requireNonNull(id);

        String key = "controlify.binding." + id.getNamespace() + "." + id.getPath();
        if (suffix != null) key += "." + suffix;

        if (notExistToNull && !Language.getInstance().has(key))
            return null;

        return Component.translatable(key);
    }
}
