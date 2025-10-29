package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.gesture.Gesture;

import java.util.Optional;

public interface GestureBuilder<G extends Gesture, B extends GestureBuilder<G, B>> {
    G build() throws IncompleteBuildException;

    /**
     * Returns a builder with only the properties that differ between this builder and the other builder.
     * This builder's properties take precedence over the other builder's properties.
     * @param other the other builder to compare with
     * @return a new builder instance with only the differing properties
     */
    B delta(GestureBuilder<?, ?> other);

    /**
     * Merges the properties of this builder with another builder of the same type.
     * The properties of the other builder take precedence over this builder's properties.
     * @param other the other builder to merge with
     * @return a new builder instance with merged properties
     */
    Optional<B> merge(GestureBuilder<?, ?> other);

    GestureBuilderType<B> type();

    MapCodec<GestureBuilder<?, ?>> MAP_CODEC = GestureBuilderType.createCodec(GestureBuilderType.TYPES, GestureBuilderType::mapCodec, GestureBuilder::type, "type");
    Codec<GestureBuilder<?, ?>> CODEC = MAP_CODEC.codec();
}
