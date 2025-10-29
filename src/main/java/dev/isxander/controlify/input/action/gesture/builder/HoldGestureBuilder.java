package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.gesture.HoldGesture;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record HoldGestureBuilder(Optional<ResourceLocation> input) implements GestureBuilder<HoldGesture, HoldGestureBuilder> {

    @Override
    public HoldGesture build() {
        return new HoldGesture(this.input().orElseThrow());
    }

    @Override
    public Optional<HoldGestureBuilder> merge(GestureBuilder<?, ?> other) throws IncompatibleMergeException {
        if (other instanceof HoldGestureBuilder(Optional<ResourceLocation> otherInput)) {
            Optional<ResourceLocation> thisInput = this.input();
            return Optional.of(new HoldGestureBuilder(otherInput.or(() -> thisInput)));
        }
        return Optional.empty();
    }

    @Override
    public HoldGestureBuilder delta(GestureBuilder<?, ?> other) {
        if (!(other instanceof HoldGestureBuilder hg)) return this;
        return new HoldGestureBuilder(
                this.input().equals(hg.input()) ? Optional.empty() : this.input()
        );
    }

    @Override
    public GestureBuilderType<HoldGestureBuilder> type() {
        return GestureBuilderType.HOLD;
    }

    public static final String GESTURE_ID = "hold";
    public static final MapCodec<HoldGestureBuilder> MAP_CODEC = ResourceLocation.CODEC
            .optionalFieldOf("input")
            .xmap(HoldGestureBuilder::new, HoldGestureBuilder::input);
    public static final Codec<HoldGestureBuilder> CODEC = MAP_CODEC.codec();
}
