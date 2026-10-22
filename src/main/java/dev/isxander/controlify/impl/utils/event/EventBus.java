package dev.isxander.controlify.impl.utils.event;

import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.EventSource;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/// Synchronous subscription-order dispatch with a snapshot of listeners per emission.
/// Subscription changes apply to later emissions; listener exceptions propagate.
public class EventBus<T> implements EventSource<T> {
	private final CopyOnWriteArrayList<Registration> listeners = new CopyOnWriteArrayList<>();

	/// Notifies subscribers without forwarding to another stage.
	public void emit(T event) {
		for (Registration registration : this.listeners) {
			registration.listener.accept(event);
		}
	}

	@Override
	public @NonNull Subscription subscribe(@NonNull EventSink<? super T> listener) {
		Registration registration = new Registration(Objects.requireNonNull(listener, "listener"));
		this.listeners.add(registration);
		return registration;
	}

	private final class Registration implements Subscription {
		private final EventSink<? super T> listener;

		private Registration(EventSink<? super T> listener) {
			this.listener = listener;
		}

		@Override
		public void close() {
			EventBus.this.listeners.remove(this);
		}
	}
}
