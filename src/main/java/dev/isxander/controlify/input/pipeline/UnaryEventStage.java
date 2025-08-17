package dev.isxander.controlify.input.pipeline;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Represents a stage in an event processing pipeline that takes an event of type {@code T}
 * and produces an event of the same type, potentially filtering, modifying, or anything.
 * @param <T> the type of events this stage handles
 */
@FunctionalInterface
public interface UnaryEventStage<T> extends EventStage<T, T> {
    default UnaryEventStage<T> andThen(UnaryEventStage<T> next) {
        return (e, out) -> this.onEvent(e, x -> next.onEvent(x, out));
    }

    static <T> UnaryEventStage<T> filter(Predicate<? super T> p) {
        return (e, out) -> { if (p.test(e)) out.accept(e); };
    }

    static <T> UnaryEventStage<T> map(UnaryOperator<T> f) {
        return (e, out) -> out.accept(f.apply(e));
    }

    static <T> UnaryEventStage<T> flatMap(
            Function<? super T, ? extends Iterable<? extends T>> f) {
        return (e, out) -> { for (var x : f.apply(e)) out.accept(x); };
    }
}
