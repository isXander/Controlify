package dev.isxander.controlify.input.pipeline;

/**
 * Pipes events from an {@link EventSink} to an {@link EventSource} via an {@link EventStage}.
 * @param <I> the type of input events
 * @param <O> the type of output events
 */
public final class EventPipe<I, O>
        implements EventSink<I>, EventSource<O> {

    private final EventStage<I, O> stage;
    private final EventBus<O> bus;

    public EventPipe(EventStage<I, O> stage) {
        this.stage = stage;
        this.bus = new SimpleEventBus<>();
    }

    @Override
    public void accept(I event) {
        stage.onEvent(event, bus);
    }

    @Override
    public EventSubscription subscribe(EventSink<? super O> sink) {
        return bus.subscribe(sink);
    }
}
