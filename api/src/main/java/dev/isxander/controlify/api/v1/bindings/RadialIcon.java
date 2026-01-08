package dev.isxander.controlify.api.v1.bindings;

import dev.isxander.controlify.api.CID;

public sealed interface RadialIcon {
    record Item(CID id) implements RadialIcon {}
    record PotionEffect(CID id) implements RadialIcon {}
}
