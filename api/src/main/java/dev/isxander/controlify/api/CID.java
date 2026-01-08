package dev.isxander.controlify.api;

import org.jspecify.annotations.NonNull;

/**
 * Equivalent to Minecraft's Identifier/ResourceLocation
 * @param namespace the namespace
 * @param path the path
 */
public record CID(String namespace, String path) {
    @Override
    public @NonNull String toString() {
        return namespace + ":" + path;
    }
}
