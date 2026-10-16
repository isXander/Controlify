package dev.isxander.controlify.utils.event;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/// Notifies listeners synchronously on the emitting thread before forwarding an event.
/// Each emission uses a snapshot of subscriptions; changes made during a callback
/// apply to subsequent emissions. Listener exceptions propagate to the caller.
public class EventTap<T> implements EventSource<T>, UnaryEventStage<T> {
	private final CopyOnWriteArrayList<Registration> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void onEvent(T event, EventSink<? super T> downstream) {
		this.emit(event);
		downstream.accept(event);
	}

	/// Notifies subscribers without forwarding to another stage.
	public void emit(T event) {
		for (Registration registration : this.listeners) {
			registration.listener.accept(event);
		}
	}

	@Override
	public Subscription subscribe(EventSink<? super T> listener) {
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
			EventTap.this.listeners.remove(this);
		}
	}
}
