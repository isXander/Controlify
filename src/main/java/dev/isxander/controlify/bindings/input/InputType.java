package dev.isxander.controlify.bindings.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import dev.isxander.controlify.utils.codec.FuzzyMapCodec;
import dev.isxander.controlify.utils.codec.StrictEitherMapCodec;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public record InputType<T extends Input>(String id, MapCodec<T> codec) implements StringRepresentable {
    public static final InputType<ButtonInput> BUTTON = new InputType<>(ButtonInput.INPUT_ID, ButtonInput.CODEC);
    public static final InputType<AxisInput> AXIS = new InputType<>(AxisInput.INPUT_ID, AxisInput.CODEC);
    public static final InputType<HatInput> HAT = new InputType<>(HatInput.INPUT_ID, HatInput.CODEC);
    public static final InputType<EmptyInput> EMPTY = new InputType<>(EmptyInput.INPUT_ID, EmptyInput.CODEC);

    public static final InputType<?>[] TYPES = {
        InputType.BUTTON, InputType.AXIS, InputType.HAT, InputType.EMPTY
    };

    public static <T extends StringRepresentable, E> MapCodec<E> createCodec(
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

    @Override
    public @NotNull String getSerializedName() {
        return this.id();
    }
}
