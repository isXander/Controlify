package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.gesture.NoopGesture;

import java.util.Optional;

public record NoopGestureBuilder() implements GestureBuilder<NoopGesture, NoopGestureBuilder> {
    public static final NoopGestureBuilder INSTANCE = new NoopGestureBuilder();

    @Override
    public NoopGesture build() {
        return new NoopGesture();
    }

    @Override
    public Optional<NoopGestureBuilder> merge(GestureBuilder<?, ?> other) throws IncompatibleMergeException {
        if (other instanceof NoopGestureBuilder n) {
            return Optional.of(n);
        }
        return Optional.empty();
    }

    @Override
    public NoopGestureBuilder delta(GestureBuilder<?, ?> other) {
        return this;
    }

    @Override
    public GestureBuilderType<NoopGestureBuilder> type() {
        return GestureBuilderType.NOOP;
    }

    public static final String GESTURE_ID = "noop";
    public static final MapCodec<NoopGestureBuilder> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final Codec<NoopGestureBuilder> CODEC = MAP_CODEC.codec();
}
