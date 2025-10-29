package dev.isxander.controlify.input.pipeline;

/**
 * An event stage that processes an input event of type {@code I}
 * and produces none or many output events of type {@code O}.
 * The output events are sent to the provided {@link EventSink}.
 * This stage is paced by a {@link Clock}, which controls when the stage is allowed to produce output events.
 * @param <I> the type of events this stage accepts
 * @param <O> the type of events this stage produces
 */
public interface PacedEventStage<I, O> {
    void onEvent(I event, EventSink<? super O> downstream);

    EventSubscription attachClock(Clock clock, EventSink<? super O> downstream);
}
