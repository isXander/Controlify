package dev.isxander.controlify.api.v1.bindings;

import dev.isxander.controlify.api.CIdentifier;

public sealed interface RadialIcon {
    record Item(CIdentifier id) implements RadialIcon {}
    record PotionEffect(CIdentifier id) implements RadialIcon {}
}
