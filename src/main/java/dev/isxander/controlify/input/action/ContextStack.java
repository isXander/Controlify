package dev.isxander.controlify.input.action;

import java.util.Set;

public record ContextStack(Set<Context> ordered) {
    public static final ContextStack NONE = new ContextStack(Set.of());
}
