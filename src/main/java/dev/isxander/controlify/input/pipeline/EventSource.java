package dev.isxander.controlify.input.pipeline;

import java.util.List;
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

    /** Connect to a terminal sink. */
    default EventSubscription to(EventSink<? super T> sink) throws Exception {
        return subscribe(sink);
    }

    /** Non-intrusive side-effect observer. */
    default EventSource<T> tap(EventTap<? super T> tap) {
        return out -> subscribe(e -> { tap.onEvent(e); out.accept(e); });
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
