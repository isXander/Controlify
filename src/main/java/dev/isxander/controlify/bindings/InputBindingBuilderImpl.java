package dev.isxander.controlify.bindings;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBindingBuilder;
import dev.isxander.controlify.bindings.defaults.DefaultBindProvider;
import dev.isxander.controlify.bindings.input.EmptyInput;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class InputBindingBuilderImpl implements InputBindingBuilder {
    private @Nullable Identifier id;
    private @Nullable Component category;
    private @Nullable Component customName, customDescription;
    private @Nullable Input defaultInput;
    private final Set<BindContext> allowedContexts = new HashSet<>();
    private @Nullable Identifier radialCandidate;

    private final Set<KeyMapping> keyCorrelations = new HashSet<>();
    private KeyMapping keyEmulation = null;
    private Function<ControllerEntity, Boolean> keyEmulationToggle = null;

    private boolean locked;

    @Override
    public InputBindingBuilder id(@NotNull Identifier rl) {
        checkLocked();

        this.id = rl;
        return this;
    }

    @Override
    public InputBindingBuilder id(@NotNull String namespace, @NotNull String path) {
        return this.id(Identifier.fromNamespaceAndPath(namespace, path));
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
    public InputBindingBuilder radialCandidate(@Nullable Identifier icon) {
        checkLocked();

        this.radialCandidate = icon;
        return this;
    }

    @Override
    public InputBindingBuilder addKeyCorrelation(@NotNull KeyMapping keyMapping) {
        checkLocked();

        this.keyCorrelations.add(keyMapping);
        return this;
    }

    @Override
    public InputBindingBuilder keyEmulation(@NotNull KeyMapping keyMapping, @Nullable Function<ControllerEntity, Boolean> toggleCondition) {
        checkLocked();

        this.keyEmulation = keyMapping;
        this.keyEmulationToggle = toggleCondition;
        this.addKeyCorrelation(keyMapping);
        return this;
    }

    @Override
    public InputBindingBuilder keyEmulation(@NotNull KeyMapping keyMapping) {
        return keyEmulation(keyMapping, null);
    }

    public InputBindingImpl build(@Nullable ControllerEntity controller) {
        Validate.isTrue(locked, "Tried to build builder before it was locked.");

        Identifier controllerType = controller != null
                ? controller.info().type().namespace()
                : ControllerType.DEFAULT.namespace();

        Component name = createDefaultString(controllerType, null, false);
        if (customName != null) name = customName;

        Component description = createDefaultString(controllerType, "desc", true);
        if (customDescription != null) description = customDescription;
        if (description == null) description = Component.empty();

        Supplier<Input> defaultSupplier = () -> {
            // retrieve every tick so the bind provider isn't cached after a resource reload
            DefaultBindProvider provider = Controlify.instance().defaultBindManager().getDefaultBindProvider(
                    controllerType
            );

            Input input = provider.getDefaultBind(id);
            if (input == null) input = defaultInput;
            if (input == null) input = EmptyInput.INSTANCE;

            return input;
        };

        return new InputBindingImpl(
                controller != null ? controller.input().orElse(null) : null,
                controllerType,
                id,
                name,
                description,
                category,
                defaultSupplier,
                allowedContexts,
                radialCandidate
        );
    }

    @NotNull
    public Identifier getIdAndLock() {
        checkLocked();

        locked = true;
        Validate.notNull(this.id, "Must call `.id(Identifier)` on builder!");
        Validate.notNull(this.category, "Must call `.category(Component)` on builder %s!".formatted(this.id));

        return this.id;
    }

    public Set<KeyMapping> getKeyCorrelations() {
        return this.keyCorrelations;
    }

    public @Nullable KeyMapping getKeyEmulation() {
        return this.keyEmulation;
    }

    public @Nullable Function<ControllerEntity, Boolean> getKeyEmulationToggle() {
        return this.keyEmulationToggle;
    }

    private void checkLocked() {
        Validate.isTrue(!locked, "Tried to modify binding builder after is has been locked!");
    }

    private Component createDefaultString(Identifier controllerType, @Nullable String suffix, boolean notExistToNull) {
        Objects.requireNonNull(id);

        String typeSpecificKey = controllerType.toLanguageKey("controlify.binding", id.toLanguageKey());
        if (suffix != null) typeSpecificKey += "." + suffix;
        if (Language.getInstance().has(typeSpecificKey)) {
            return Component.translatable(typeSpecificKey);
        }

        String genericKey = id.toLanguageKey("controlify.binding");
        if (suffix != null) genericKey += "." + suffix;

        if (notExistToNull && !Language.getInstance().has(genericKey))
            return null;

        return Component.translatable(genericKey);
    }
}
