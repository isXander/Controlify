package dev.isxander.controlify.api.utils.event;

import java.util.function.Predicate;

/// Subscription-only source. Controlify events dispatch synchronously in subscription
/// order on the emitting thread, without replay. Subscribe in ControlifyEntrypoint for
/// one-shot lifecycle events. Listener exceptions propagate and stop that emission.
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
		/// Unsubscribe this registration; repeated calls have no effect.
		@Override
		void close(); // unsubscribe; no checked exception
	}
}
