package dev.isxander.controlify.api.guide;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a single fact that rules can depend on.
 *
 * @param provider the provider of the fact, which is a function that takes a domain context and returns true if the fact is true
 */
public record Fact<T extends FactCtx>(ResourceLocation id, FactProvider<T> provider) {

    public static <Z extends FactCtx> Fact<Z> of(ResourceLocation id, FactProvider<Z> provider) {
        return new Fact<>(id, provider);
    }

    public static <Z extends FactCtx> Fact<Z> of(ResourceLocation id) {
        return new Fact<>(id, FactProvider.staticProvider(false));
    }
}
