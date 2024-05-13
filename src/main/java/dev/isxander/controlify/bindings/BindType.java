package dev.isxander.controlify.bindings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapEncoder;
import dev.isxander.controlify.utils.FuzzyMapCodec;
import dev.isxander.controlify.utils.StrictEitherMapCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Stream;

public record BindType<T extends Bind>(String id, MapCodec<T> codec) implements StringRepresentable {
    public static final BindType<ButtonBind> BUTTON = new BindType<>(ButtonBind.BIND_ID, ButtonBind.CODEC);
    public static final BindType<AxisBind> AXIS = new BindType<>(AxisBind.BIND_ID, AxisBind.CODEC);
    public static final BindType<HatBind> HAT = new BindType<>(HatBind.BIND_ID, HatBind.CODEC);
    public static final BindType<EmptyBind> EMPTY = new BindType<>(EmptyBind.BIND_ID, EmptyBind.CODEC);

    public static final BindType<?>[] TYPES = {
        BindType.BUTTON, BindType.AXIS, BindType.HAT, BindType.EMPTY,
    };

    public static <T extends StringRepresentable, E> MapCodec<E> createCodec(
            T[] types, Function<T, MapCodec<? extends E>> codecGetter, Function<E, T> typeGetter, String typeFieldName
    ) {
        MapCodec<E> fuzzyCodec = new FuzzyMapCodec<>(
                Stream.of(types).map(codecGetter).toList(),
                obj -> codecGetter.apply(typeGetter.apply(obj))
        );

        Codec<T> typeCodec = StringRepresentable.fromValues(() -> types);

        MapCodec<E> typedCodec = typeCodec.dispatchMap("type", typeGetter, codecGetter);
        MapCodec<E> eitherCodec = new StrictEitherMapCodec<>("type", typedCodec, fuzzyCodec, false);

        return ExtraCodecs.orCompressed(eitherCodec, typedCodec);
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id();
    }
}
