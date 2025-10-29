package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.input.action.gesture.Gesture;
import org.jetbrains.annotations.Nullable;

public final class GestureDecoderHelper {
    private GestureBuilder<?, ?> builder;

    public GestureDecoderHelper() {
        this.builder = null;
    }

    public GestureDecoderHelper(@Nullable GestureDecoderHelper other) {
        this.builder = other != null ? other.builder : null;
    }

    public void push(Dynamic<?> input) {
        GestureBuilder<?, ?> builder = parseInput(input);

        if (this.builder == null) {
            try {
                builder.build();
            } catch (IncompleteBuildException e) {
                throw new IllegalStateException("The first builder pushed must be complete", e);
            }
            this.builder = builder;
        } else {
            this.builder.merge(builder)
                    .ifPresent(gb -> this.builder = gb);
        }
    }

    public Gesture build() {
        if (this.builder == null) {
            throw new IllegalStateException("No builder has been pushed");
        }
        return this.builder.build();
    }

    private GestureBuilder<?, ?> parseInput(Dynamic<?> input) {
        if (this.builder == null) {
            return parseAsNew(input);
        } else {
            GestureBuilder<?, ?> typed = this.builder.type().codec().parse(input).result().orElse(null);
            if (typed != null) {
                return typed;
            } else {
                return parseAsNew(input);
            }
        }
    }

    private GestureBuilder<?, ?> parseAsNew(Dynamic<?> input) {
        return GestureBuilder.CODEC.parse(input).getOrThrow();
    }
}
