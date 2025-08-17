package dev.isxander.controlify.input.pipeline;

/**
 * Represents an observer of a source of events.
 * @param <T> the type of events this tap handles
 */
@FunctionalInterface
public interface EventTap<T> {
    void onEvent(T event);
}
