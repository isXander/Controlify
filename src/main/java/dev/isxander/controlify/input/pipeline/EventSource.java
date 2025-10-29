package dev.isxander.controlify.input.pipeline;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A source of events {@link EventSink sinks} can subscribe to.
 * @param <T> the type of events this source emits
 */
@FunctionalInterface
public interface EventSource<T> {
    EventSubscription subscribe(EventSink<? super T> sink);

    /** Pipe through a stage. */
    default <O> EventSource<O> via(EventStage<? super T, O> stage) {
        return out -> subscribe(e -> stage.onEvent(e, out));
    }

    default <O> EventSource<O> via(Clock clock, PacedEventStage<? super T, O> stage) {
        return out -> {
            EventSubscription dataSub = subscribe(e -> stage.onEvent(e, out));
            EventSubscription clockSub = stage.attachClock(clock, out);
            return () -> {
                clockSub.cancel();
                dataSub.cancel();
            };
        };
    }

    /** Non-intrusive side-effect observer. */
    default EventSource<T> tap(EventSink<? super T> tap) {
        return out -> subscribe(e -> { tap.accept(e); out.accept(e); });
    }

    default <R> EventSource<R> flatMap(Function<T, Stream<R>> mapper) {
        return out -> subscribe(e -> mapper.apply(e).forEach(out::accept));
    }

    default EventSource<T> filter(Predicate<? super T> predicate) {
        return out -> subscribe(e -> {
            if (predicate.test(e)) {
                out.accept(e);
            }
        });
    }

    default <R extends T> EventSource<R> filter(Class<R> type) {
        return out -> subscribe(e -> {
            if (type.isInstance(e)) {
                out.accept(type.cast(e));
            }
        });
    }

    default EventSource<T> merge(EventSource<? extends T> other) {
        return merge(this, other);
    }

    /**
     * Creates an {@link EventBus} that forwards all events from this source and also
     * allows you to inject additional events via {@link EventBus#accept(Object)}.
     * <p>
     * Subscribers that subscribe to the returned bus will receive both:
     * <ol>
     *     <li>events emitted by this source</li>
     *     <li>any events injected into the bus</li>
     * </ol>
     * <strong>Note: subscribers of this original source are not affected by injected events.</strong>
     *
     * @return an event bus that mirrors this source and accepts injected events
     */
    default EventBus<T> toBus() {
        var bus = new SimpleEventBus<T>();
        this.subscribe(bus);
        return bus;
    }

    @SafeVarargs
    static <T> EventSource<T> merge(EventSource<? extends T>... sources) {
        return sink -> {
            List<EventSubscription> handles = Stream.of(sources)
                    .map(s -> s.subscribe(sink))
                    .toList();

            return () -> handles.forEach(EventSubscription::cancel);
        };
    }
}
