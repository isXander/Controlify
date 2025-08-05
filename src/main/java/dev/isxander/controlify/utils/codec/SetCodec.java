package dev.isxander.controlify.utils.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public record SetCodec<E>(Codec<E> elementCodec, int minSize, int maxSize) implements Codec<Set<E>> {

    public SetCodec(final Codec<E> elementCodec) {
        this(elementCodec, 0, Integer.MAX_VALUE);
    }

    private <R> DataResult<R> createTooShortError(final int size) {
        return DataResult.error(() -> "Set is too short: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
    }

    private <R> DataResult<R> createTooLongError(final int size) {
        return DataResult.error(() -> "Set is too long: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
    }

    private <R> DataResult<R> createDuplicatesError() {
        return DataResult.error(() -> "Set contains duplicate elements");
    }

    @Override
    public <T> DataResult<T> encode(Set<E> input, DynamicOps<T> ops, T prefix) {
        if (input.size() < minSize) {
            return createTooShortError(input.size());
        }
        if (input.size() > maxSize) {
            return createTooLongError(input.size());
        }
        ListBuilder<T> builder = ops.listBuilder();
        for (E element : input) {
            builder.add(elementCodec.encodeStart(ops, element));
        }
        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<Set<E>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
            final DecoderState<T> decoder = new DecoderState<>(ops);
            stream.accept(decoder::accept);
            return decoder.build();
        });
    }

    @Override
    public @NotNull String toString() {
        return "SetCodec[" + elementCodec + ']';
    }

    private class DecoderState<T> {
        private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

        private final DynamicOps<T> ops;
        private final Set<E> elements = new LinkedHashSet<>();
        private final Stream.Builder<T> failed = Stream.builder();
        private DataResult<Unit> result = INITIAL_RESULT;
        private int totalCount;

        private DecoderState(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        public void accept(final T value) {
            totalCount++;
            if (elements.size() >= maxSize) {
                failed.add(value);
                return;
            }
            final DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);
            elementResult.error().ifPresent(error -> failed.add(value));
            int oldSize = elements.size();
            elementResult.resultOrPartial().ifPresent(pair -> elements.add(pair.getFirst()));
            if (elements.size() == oldSize) {
                failed.add(value);
                result = createDuplicatesError();
                return;
            }
            result = result.apply2stable((result, element) -> result, elementResult);
        }

        public DataResult<Pair<Set<E>, T>> build() {
            if (elements.size() < minSize) {
                return createTooShortError(elements.size());
            }
            final T errors = ops.createList(failed.build());
            final Pair<Set<E>, T> pair = Pair.of(Set.copyOf(elements), errors);
            if (totalCount > maxSize) {
                result = createTooLongError(totalCount);
            }
            return result.map(ignored -> pair).setPartial(pair);
        }
    }
}
