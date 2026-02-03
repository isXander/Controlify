package dev.isxander.controlify.api.guide;

import net.minecraft.resources.Identifier;

/**
 * Represents a single fact that rules can depend on.
 *
 * @param provider the provider of the fact, which is a function that takes a domain context and returns true if the fact is true
 */
public record Fact<T extends FactCtx>(Identifier id, FactProvider<T> provider) {

    public static <Z extends FactCtx> Fact<Z> of(Identifier id, FactProvider<Z> provider) {
        return new Fact<>(id, provider);
    }

    public static <Z extends FactCtx> Fact<Z> of(Identifier id) {
        return new Fact<>(id, FactProvider.staticProvider(false));
    }
}
