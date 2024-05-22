package dev.isxander.controlify.bindings.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.FuzzyMapCodec;
import dev.isxander.controlify.utils.StrictEitherMapCodec;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
        MapCodec<E> fuzzyCodec = new FuzzyMapCodec<>(
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

        MapCodec<E> typedCodec = typeCodec.dispatchMap(
                typeFieldName,
                typeGetter,
                /*? if >1.20.4 {*/
                codecGetter
                /*?} else {*//*
                stringRepresentable -> codecGetter.apply(stringRepresentable).codec()
                *//*?}*/
        );
        MapCodec<E> eitherCodec = new StrictEitherMapCodec<>(typeFieldName, typedCodec, fuzzyCodec, false);

        return CUtil.orCompressed(eitherCodec, typedCodec);
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id();
    }
}
