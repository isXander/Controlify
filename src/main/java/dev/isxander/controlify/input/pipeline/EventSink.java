package dev.isxander.controlify.input.pipeline;

/**
 * Accepts events of type {@code T}.
 * @param <T> the type of events this sink handles
 */
@FunctionalInterface
public interface EventSink<T> {
    void accept(T event);
}
