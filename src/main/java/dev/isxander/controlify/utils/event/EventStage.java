package dev.isxander.controlify.utils.event;

@FunctionalInterface
public interface EventStage<I, O> {
	void onEvent(I event, EventSink<? super O> downstream);

	default <T> EventStage<I, T> andThen(EventStage<O, T> next) {
		return (event, downstream) -> this.onEvent(event, x -> next.onEvent(x, downstream));
	}

	static <T> UnaryEventStage<T> identity() {
		return (event, downstream) -> downstream.accept(event);
	}
}
