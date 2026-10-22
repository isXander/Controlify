/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils.event;

import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.api.utils.event.UnaryEventStage;
import org.jspecify.annotations.NonNull;

/// Notifies listeners synchronously on the emitting thread before forwarding an event.
/// Each emission uses a snapshot of subscriptions; changes made during a callback
/// apply to subsequent emissions. Listener exceptions propagate to the caller.
public class EventTap<T> implements EventSource<T>, UnaryEventStage<T> {
	private final EventBus<T> bus = new EventBus<>();

	@Override
	public void onEvent(@NonNull T event, @NonNull EventSink<? super T> downstream) {
		this.emit(event);
		downstream.accept(event);
	}

	public void emit(T event) {
		this.bus.emit(event);
	}

	@Override
	public @NonNull Subscription subscribe(@NonNull EventSink<? super T> listener) {
		return this.bus.subscribe(listener);
	}
}
