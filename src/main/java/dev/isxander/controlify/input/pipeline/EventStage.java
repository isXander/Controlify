package dev.isxander.controlify.input.pipeline;

/**
 * An event stage that processes an input event of type {@code I}
 * and produces none or many output events of type {@code O}.
 * The output events are sent to the provided {@link EventSink}.
 * @param <I> the type of events this stage accepts
 * @param <O> the type of events this stage produces
 */
@FunctionalInterface
public interface EventStage<I, O> {
    void onEvent(I event, EventSink<? super O> downstream);

    default EventPipe<I, O> pipe() {
        return new EventPipe<>(this);
    }

    static <T> UnaryEventStage<T> identity() {
        return (event, out) -> out.accept(event);
    }
}
