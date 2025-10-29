package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import dev.isxander.controlify.utils.codec.StrictEitherMapCodec;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public record GestureBuilderType<T extends GestureBuilder<?, ?>>(String id, MapCodec<T> mapCodec, Codec<T> codec) implements StringRepresentable {
    public static final GestureBuilderType<ChordGestureBuilder> CHORD = new GestureBuilderType<>(ChordGestureBuilder.GESTURE_ID, ChordGestureBuilder.MAP_CODEC, ChordGestureBuilder.CODEC);
    public static final GestureBuilderType<ContinuousGestureBuilder> CONTINUOUS = new GestureBuilderType<>(ContinuousGestureBuilder.GESTURE_ID, ContinuousGestureBuilder.MAP_CODEC, ContinuousGestureBuilder.CODEC);
    public static final GestureBuilderType<DoubleTapGestureBuilder> DOUBLE_TAP = new GestureBuilderType<>(DoubleTapGestureBuilder.GESTURE_ID, DoubleTapGestureBuilder.MAP_CODEC, DoubleTapGestureBuilder.CODEC);
    public static final GestureBuilderType<GuiPressGestureBuilder> GUI_PRESS = new GestureBuilderType<>(GuiPressGestureBuilder.GESTURE_ID, GuiPressGestureBuilder.MAP_CODEC, GuiPressGestureBuilder.CODEC);
    public static final GestureBuilderType<HoldGestureBuilder> HOLD = new GestureBuilderType<>(HoldGestureBuilder.GESTURE_ID, HoldGestureBuilder.MAP_CODEC, HoldGestureBuilder.CODEC);
    public static final GestureBuilderType<LatchRepeatPulseGestureBuilder> LATCH_REPEAT_PULSE = new GestureBuilderType<>(LatchRepeatPulseGestureBuilder.GESTURE_ID, LatchRepeatPulseGestureBuilder.MAP_CODEC, LatchRepeatPulseGestureBuilder.CODEC);
    public static final GestureBuilderType<NoopGestureBuilder> NOOP = new GestureBuilderType<>(NoopGestureBuilder.GESTURE_ID, NoopGestureBuilder.MAP_CODEC, NoopGestureBuilder.CODEC);
    public static final GestureBuilderType<TapGestureBuilder> TAP = new GestureBuilderType<>(TapGestureBuilder.GESTURE_ID, TapGestureBuilder.MAP_CODEC, TapGestureBuilder.CODEC);
    public static final GestureBuilderType<ToggleGestureBuilder> TOGGLE = new GestureBuilderType<>(ToggleGestureBuilder.GESTURE_ID, ToggleGestureBuilder.MAP_CODEC, ToggleGestureBuilder.CODEC);

    public static final GestureBuilderType<?>[] TYPES = {
            CHORD, CONTINUOUS, DOUBLE_TAP, GUI_PRESS, HOLD, LATCH_REPEAT_PULSE, NOOP, TAP, TOGGLE,
    };

    @Override
    public @NotNull String getSerializedName() {
        return this.id();
    }

    static <T extends StringRepresentable, E> MapCodec<E> createCodec(
            T[] types, Function<T, MapCodec<? extends E>> codecGetter, Function<E, T> typeGetter, String typeFieldName
    ) {
        MapCodec<E> fuzzyCodec = CExtraCodecs.fuzzyMap(
                Stream.of(types).map(codecGetter).toList(),
                obj -> codecGetter.apply(typeGetter.apply(obj))
        );


        Codec<T> typeCodec = ExtraCodecs.orCompressed(
                CUtil.stringResolver(
                        StringRepresentable::getSerializedName,
                        CUtil.createNameLookup(types, Function.identity())
                ),
                ExtraCodecs.idResolverCodec(
                        Util.createIndexLookup(Arrays.asList(types)),
                        i -> i >= 0 && i < types.length ? types[i] : null,
                        -1
                )
        );

        MapCodec<E> typedCodec = typeCodec.dispatchMap(typeFieldName, typeGetter, codecGetter);
        MapCodec<E> eitherCodec = new StrictEitherMapCodec<>(typeFieldName, typedCodec, fuzzyCodec, false);

        return CUtil.orCompressed(eitherCodec, typedCodec);
    }
}
