package dev.isxander.controlify.utils.event;

import java.util.function.Predicate;

public interface EventSource<T> {
	Subscription subscribe(EventSink<? super T> listener);

	default <O> EventSource<O> via(EventStage<T, O> stage) {
		return sink -> this.subscribe(stage.bind(sink));
	}

	default EventSource<T> filter(Predicate<T> predicate) {
		return sink -> this.subscribe(event -> {
			if (predicate.test(event)) {
				sink.accept(event);
			}
		});
	}

	@FunctionalInterface
	interface Subscription extends AutoCloseable {
		@Override
		void close(); // unsubscribe; no checked exception
	}
}
