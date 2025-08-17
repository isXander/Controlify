package dev.isxander.controlify.input.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of {@link EventBus} that stores subscribers in a list and emits events to them.
 * @param <T> the type of events this bus handles
 */
public class SimpleEventBus<T> implements EventBus<T> {
    private final List<EventSink<? super T>> subscribers = new ArrayList<>();

    @Override
    public void accept(T event) {
        for (var sub : subscribers) sub.accept(event);
    }

    @Override
    public EventSubscription subscribe(EventSink<? super T> sink) {
        subscribers.add(sink);
        return () -> subscribers.remove(sink);
    }
}
