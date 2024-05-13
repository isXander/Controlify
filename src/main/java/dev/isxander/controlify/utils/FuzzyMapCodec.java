package dev.isxander.controlify.utils;

import com.mojang.serialization.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class FuzzyMapCodec<T> extends MapCodec<T> {
    private final List<MapCodec<? extends T>> codecs;
    private final Function<T, MapEncoder<? extends T>> encoderGetter;

    public FuzzyMapCodec(List<MapCodec<? extends T>> codecs, Function<T, MapEncoder<? extends T>> encoderGetter) {
        this.codecs = codecs;
        this.encoderGetter = encoderGetter;
    }

    @Override
    public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
        for (MapDecoder<? extends T> decoder : this.codecs) {
            DataResult<? extends T> result = decoder.decode(ops, input);
            if (result.result().isPresent()) {
                return (DataResult<T>) result;
            }
        }

        return DataResult.error(() -> "No matching codec found.");
    }

    @Override
    public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
        MapEncoder<T> encoder = (MapEncoder<T>) this.encoderGetter.apply(input);
        return encoder.encode(input, ops, prefix);
    }

    @Override
    public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
        return this.codecs.stream().flatMap(codec -> codec.keys(ops)).distinct();
    }
}
