package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.gesture.ToggleGesture;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ToggleGestureBuilder(Optional<GestureBuilder<?, ?>> pulseGesture) implements GestureBuilder<ToggleGesture, ToggleGestureBuilder> {
    @Override
    public ToggleGesture build() {
        return new ToggleGesture(this.pulseGesture.orElseThrow().build());
    }

    @Override
    public Optional<ToggleGestureBuilder> merge(GestureBuilder<?, ?> other) {
        if (!(other instanceof ToggleGestureBuilder tg)) return Optional.empty();

        var mergedPulse =
                this.pulseGesture
                        .<GestureBuilder<?, ?>>flatMap(a -> tg.pulseGesture.flatMap(a::merge)) // both present → merge
                        .or(() -> tg.pulseGesture)    // otherwise prefer "other" if present
                        .or(() -> this.pulseGesture); // otherwise "this" if present

        return Optional.of(new ToggleGestureBuilder(mergedPulse));
    }

    @Override
    public ToggleGestureBuilder delta(GestureBuilder<?, ?> other) {
        if (!(other instanceof ToggleGestureBuilder tg)) return this;

        return new ToggleGestureBuilder(
                this.pulseGesture.equals(tg.pulseGesture) ? Optional.empty() : this.pulseGesture
        );
    }

    @Override
    public GestureBuilderType<ToggleGestureBuilder> type() {
        return GestureBuilderType.TOGGLE;
    }

    public static final String GESTURE_ID = "toggle";
    public static final MapCodec<ToggleGestureBuilder> MAP_CODEC = GestureBuilder.CODEC
            .optionalFieldOf("gesture")
            .xmap(ToggleGestureBuilder::new, ToggleGestureBuilder::pulseGesture);
    public static final com.mojang.serialization.Codec<ToggleGestureBuilder> CODEC = MAP_CODEC.codec();
}
