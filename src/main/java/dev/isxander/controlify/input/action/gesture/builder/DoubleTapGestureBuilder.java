package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.gesture.DoubleTapGesture;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record DoubleTapGestureBuilder(Optional<ResourceLocation> input) implements GestureBuilder<DoubleTapGesture, DoubleTapGestureBuilder> {
    @Override
    public DoubleTapGesture build() {
        return new DoubleTapGesture(this.input().orElseThrow());
    }

    @Override
    public Optional<DoubleTapGestureBuilder> merge(GestureBuilder<?, ?> other) throws IncompatibleMergeException {
        if (other instanceof DoubleTapGestureBuilder(Optional<ResourceLocation> otherInput)) {
            Optional<ResourceLocation> thisInput = this.input();
            return Optional.of(new DoubleTapGestureBuilder(otherInput.or(() -> thisInput)));
        }
        return Optional.empty();
    }

    @Override
    public DoubleTapGestureBuilder delta(GestureBuilder<?, ?> other) {
        if (!(other instanceof DoubleTapGestureBuilder dtg)) return this;
        return new DoubleTapGestureBuilder(
                this.input().equals(dtg.input()) ? Optional.empty() : this.input()
        );
    }

    @Override
    public GestureBuilderType<DoubleTapGestureBuilder> type() {
        return GestureBuilderType.DOUBLE_TAP;
    }

    public static final String GESTURE_ID = "double_tap";
    public static final MapCodec<DoubleTapGestureBuilder> MAP_CODEC = ResourceLocation.CODEC
            .optionalFieldOf("input")
            .xmap(DoubleTapGestureBuilder::new, DoubleTapGestureBuilder::input);
    public static final Codec<DoubleTapGestureBuilder> CODEC = MAP_CODEC.codec();
}
