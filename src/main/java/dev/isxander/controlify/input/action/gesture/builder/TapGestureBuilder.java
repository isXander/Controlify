package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.gesture.TapGesture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record TapGestureBuilder(Optional<ResourceLocation> input) implements GestureBuilder<TapGesture, TapGestureBuilder> {

    @Override
    public TapGesture build() {
        return new TapGesture(this.input().orElseThrow());
    }

    @Override
    public Optional<TapGestureBuilder> merge(GestureBuilder<?, ?> other) {
        if (other instanceof TapGestureBuilder(Optional<ResourceLocation> otherInput)) {
            Optional<ResourceLocation> thisInput = this.input();
            return Optional.of(new TapGestureBuilder(otherInput.or(() -> thisInput)));
        }
        return Optional.empty();
    }

    @Override
    public TapGestureBuilder delta(GestureBuilder<?, ?> other) {
        if (!(other instanceof TapGestureBuilder tg)) return this;
        return new TapGestureBuilder(
                this.input().equals(tg.input()) ? Optional.empty() : this.input()
        );
    }

    @Override
    public GestureBuilderType<TapGestureBuilder> type() {
        return GestureBuilderType.TAP;
    }

    public static final String GESTURE_ID = "tap";
    public static final MapCodec<TapGestureBuilder> MAP_CODEC = ResourceLocation.CODEC
            .optionalFieldOf("input")
            .xmap(TapGestureBuilder::new, TapGestureBuilder::input);
    public static final Codec<TapGestureBuilder> CODEC = MAP_CODEC.codec();
}
