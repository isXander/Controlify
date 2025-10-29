package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.gesture.ContinuousGesture;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ContinuousGestureBuilder(Optional<ResourceLocation> input) implements GestureBuilder<ContinuousGesture, ContinuousGestureBuilder> {
    @Override
    public ContinuousGesture build() throws IncompleteBuildException {
        return new ContinuousGesture(this.input().orElseThrow(() -> new IncompleteBuildException("'input' is required for ContinuousGesture")));
    }

    @Override
    public Optional<ContinuousGestureBuilder> merge(GestureBuilder<?, ?> other) throws IncompatibleMergeException {
        if (other instanceof ContinuousGestureBuilder(Optional<ResourceLocation> otherInput)) {
            Optional<ResourceLocation> thisInput = this.input();
            return Optional.of(new ContinuousGestureBuilder(otherInput.or(() -> thisInput)));
        }
        return Optional.empty();
    }

    @Override
    public ContinuousGestureBuilder delta(GestureBuilder<?, ?> other) {
        if (!(other instanceof ContinuousGestureBuilder cg)) return this;
        return new ContinuousGestureBuilder(
                this.input().equals(cg.input()) ? Optional.empty() : this.input()
        );
    }

    @Override
    public GestureBuilderType<ContinuousGestureBuilder> type() {
        return GestureBuilderType.CONTINUOUS;
    }

    public static final String GESTURE_ID = "continuous";
    public static final MapCodec<ContinuousGestureBuilder> MAP_CODEC = ResourceLocation.CODEC
            .optionalFieldOf("input")
            .xmap(ContinuousGestureBuilder::new, ContinuousGestureBuilder::input);
    public static final Codec<ContinuousGestureBuilder> CODEC = MAP_CODEC.codec();
}
