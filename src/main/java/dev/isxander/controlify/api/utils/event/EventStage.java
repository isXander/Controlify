package dev.isxander.controlify.api.utils.event;

@FunctionalInterface
public interface EventStage<I, O> {
	void onEvent(I event, EventSink<? super O> downstream);

	default <T> EventStage<I, T> andThen(EventStage<? super O, ? extends T> next) {
		EventStage<I, O> first = this;
		return new EventStage<>() {
			@Override
			public void onEvent(I event, EventSink<? super T> downstream) {
				first.onEvent(event, x -> next.onEvent(x, downstream));
			}

			@Override
			public EventSink<I> bind(EventSink<? super T> downstream) {
				return first.bind(next.bind(downstream));
			}
		};
	}

	/// Connects this stage and any composed stages to a fixed downstream sink once.
	default EventSink<I> bind(EventSink<? super O> downstream) {
		return event -> this.onEvent(event, downstream);
	}

	default EventSink<I> discardDownstream() {
		return this.bind(_ -> {});
	}

	static <T> UnaryEventStage<T> identity() {
		return (event, downstream) -> downstream.accept(event);
	}
}
