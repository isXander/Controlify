package dev.isxander.controlify.utils.event;

public interface EventSource<T> {
	Subscription subscribe(EventSink<? super T> listener);

	@FunctionalInterface
	interface Subscription extends AutoCloseable {
		@Override
		void close(); // unsubscribe; no checked exception
	}
}
