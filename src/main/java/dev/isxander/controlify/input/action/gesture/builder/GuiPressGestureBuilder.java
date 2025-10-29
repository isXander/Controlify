package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.input.action.gesture.GuiPressGesture;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record GuiPressGestureBuilder(Optional<ResourceLocation> input) implements GestureBuilder<GuiPressGesture, GuiPressGestureBuilder> {

    @Override
    public GuiPressGesture build() {
        return new GuiPressGesture(this.input().orElseThrow());
    }

    @Override
    public Optional<GuiPressGestureBuilder> merge(GestureBuilder<?, ?> other) throws IncompatibleMergeException {
        if (other instanceof GuiPressGestureBuilder(Optional<ResourceLocation> otherInput)) {
            Optional<ResourceLocation> thisInput = this.input();
            return Optional.of(new GuiPressGestureBuilder(otherInput.or(() -> thisInput)));
        }
        return Optional.empty();
    }

    @Override
    public GuiPressGestureBuilder delta(GestureBuilder<?, ?> other) {
        if (!(other instanceof GuiPressGestureBuilder gpg)) return this;
        return new GuiPressGestureBuilder(
                this.input().equals(gpg.input()) ? Optional.empty() : this.input()
        );
    }

    @Override
    public GestureBuilderType<GuiPressGestureBuilder> type() {
        return GestureBuilderType.GUI_PRESS;
    }

    public static final String GESTURE_ID = "gui_press";
    public static final MapCodec<GuiPressGestureBuilder> MAP_CODEC = ResourceLocation.CODEC
            .optionalFieldOf("input")
            .xmap(GuiPressGestureBuilder::new, GuiPressGestureBuilder::input);
    public static final Codec<GuiPressGestureBuilder> CODEC = MAP_CODEC.codec();
}
