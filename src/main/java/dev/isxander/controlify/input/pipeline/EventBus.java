package dev.isxander.controlify.input.pipeline;

/**
 * Receives events via {@link EventSink} and emits to them to subscribers via {@link EventSource}.
 * @param <T> the type of events this bus handles
 */
public interface EventBus<T>
        extends EventSource<T>, EventSink<T> {
}
