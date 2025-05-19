package dev.isxander.controlify.splitscreen.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;
import java.util.function.Function;

public class CodecExtensionHelper {

    /**
     * @param original vanilla codec for base record
     * @param extensionMapOutputCodec .fieldOf(...).codec() codec for the extension
     * @param extensionGetter a function that gets the extension from the base record
     * @param extensionApplicator a threadlocal that is used as a param to the constructor of the V type
     * @return a codec that codes the base record, with an additional extension field.
     * @param <V> vanilla, base record class
     * @param <E> extension, additional class
     */
    public static <V, E> Codec<V> buildExtensionCodec(Codec<V> original, Codec<Optional<E>> extensionMapOutputCodec, Function<V, Optional<E>> extensionGetter, ThreadLocal<E> extensionApplicator) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
                return extensionMapOutputCodec.decode(ops, input)
                        .flatMap(extPair -> {
                            try {
                                extensionApplicator.set(extPair.getFirst().orElse(null));
                                return original.decode(ops, extPair.getSecond());
                            } finally {
                                extensionApplicator.remove();
                            }
                        });
            }

            @Override
            public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
                Optional<E> ext = extensionGetter.apply(input);

                return original.encode(input, ops, prefix)
                        .flatMap(p -> extensionMapOutputCodec.encode(ext, ops, p));
            }
        };
    }

}
