package dev.isxander.controlify.input.action;

public interface ActionSpecRegistry {
    void register(ActionSpec spec);

    default <H extends ActionHandle> ActionAccessor<H> register(ActionSpecBuilder<H> builder) {
        return builder.buildAndRegister(this);
    }

    ActionSpecRegistry REGISTRY = ActionSpecRegistryImpl.INSTANCE;
}
