package dev.isxander.controlify.input.pipeline;

/**
 * Represents a subscription to an {@link EventSource event source}.
 * Subscriptions can be cancelled to stop receiving events.
 *
 * @see EventSource
 * @see EventSink
 */
@FunctionalInterface
public interface EventSubscription {
    void cancel();

    default void use(Runnable r) {
        try {
            r.run();
        } finally {
            cancel();
        }
    }

    default AutoCloseable asAutoCloseable() {
        return this::cancel;
    }
}
