package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.InputComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ActionSpecBuilderImpl<H extends ActionHandle> implements ActionSpecBuilder<H> {

    private final Function<Action, H> handleFactory;
    private final ChannelKind channelKind;

    public ActionSpecBuilderImpl(
            Function<Action, H> handleFactory, ChannelKind channelKind
    ) {
        this.handleFactory = handleFactory;
        this.channelKind = channelKind;
    }

    private @Nullable ResourceLocation id = null;
    private @Nullable Component explicitName = null;
    private @Nullable Component explicitDescription = null;
    private @Nullable Component category = null;
    private final Set<ResourceLocation> contexts = new HashSet<>();
    private int priority = 0;


    @Override
    public ActionSpecBuilder<H> id(ResourceLocation id) {
        this.id = id;
        return this;
    }

    @Override
    public ActionSpecBuilder<H> name(Component name) {
        this.explicitName = name;
        return this;
    }

    @Override
    public ActionSpecBuilder<H> description(Component description) {
        this.explicitDescription = description;
        return this;
    }

    @Override
    public ActionSpecBuilder<H> category(Component category) {
        this.category = category;
        return this;
    }

    @Override
    public ActionSpecBuilder<H> context(ResourceLocation... contexts) {
        this.contexts.addAll(Arrays.asList(contexts));
        return this;
    }

    @Override
    public ActionSpecBuilder<H> priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ActionAccessor<H> buildAndRegister(ActionSpecRegistry registry) {
        Validate.notNull(this.id, "Action id must be set");
        Validate.notNull(this.category, "Action category must be set");
        Validate.isTrue(!this.contexts.isEmpty(), "At least one context must be set");

        Component name = Optional.ofNullable(this.explicitName)
                .orElseGet(() -> Component.translatable(this.id.toLanguageKey("controlify.binding")));
        Component description = Optional.ofNullable(this.explicitDescription)
                .orElseGet(() -> Component.translatable(this.id.toLanguageKey("controlify.binding") + ".desc"));

        var spec = new ActionSpec(
                this.id,
                name,
                description,
                this.category,
                this.channelKind,
                Set.copyOf(this.contexts),
                this.priority
        );

        registry.register(spec);

        return new ActionAccessor<>() {
            @Override
            public @Nullable H onOrNull(InputComponent inputComponent) {
                return handleFactory.apply(inputComponent.getAction(actionId()));
            }

            @Override
            public ResourceLocation actionId() {
                return spec.id();
            }
        };
    }
}
