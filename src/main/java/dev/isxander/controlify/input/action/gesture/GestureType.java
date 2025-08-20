package dev.isxander.controlify.input.action.gesture;

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

public record GestureType<T extends SerializableGesture<T>>(String id, MapCodec<T> mapCodec) implements StringRepresentable {
    public static final GestureType<TapGesture> TAP = new GestureType<>(TapGesture.GESTURE_ID, TapGesture.MAP_CODEC);
    public static final GestureType<DoubleTapGesture> DOUBLE_TAP = new GestureType<>(DoubleTapGesture.GESTURE_ID, DoubleTapGesture.MAP_CODEC);
    public static final GestureType<ContinuousGesture> CONTINUOUS = new GestureType<>(ContinuousGesture.GESTURE_ID, ContinuousGesture.MAP_CODEC);
    public static final GestureType<HoldGesture> HOLD = new GestureType<>(HoldGesture.GESTURE_ID, HoldGesture.MAP_CODEC);
    public static final GestureType<ChordGesture> CHORD = new GestureType<>(ChordGesture.GESTURE_ID, ChordGesture.MAP_CODEC);
    public static final GestureType<GuiPressGesture> GUI_PRESS = new GestureType<>(GuiPressGesture.GESTURE_ID, GuiPressGesture.MAP_CODEC);
    public static final GestureType<LatchRepeatPulseGesture> LATCH_REPEAT_PULSE = new GestureType<>(LatchRepeatPulseGesture.GESTURE_ID, LatchRepeatPulseGesture.MAP_CODEC);
    public static final GestureType<NoopGesture> NOOP = new GestureType<>(NoopGesture.GESTURE_ID, NoopGesture.MAP_CODEC);
    public static final GestureType<ToggleGesture> TOGGLE = new GestureType<>(ToggleGesture.GESTURE_ID, ToggleGesture.MAP_CODEC);

    public static final GestureType<?>[] TYPES = {
        TAP, DOUBLE_TAP, CONTINUOUS, HOLD, CHORD, GUI_PRESS, LATCH_REPEAT_PULSE, NOOP, TOGGLE,
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
