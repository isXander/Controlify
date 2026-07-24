/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.fabric.platform;

import dev.isxander.controlify.platform.EventHandler;
import net.fabricmc.fabric.api.event.EventFactory;

public class FabricBackedEventHandler<T> implements EventHandler<T> {
	private final net.fabricmc.fabric.api.event.Event<Callback<T>> backedEvent;

	public FabricBackedEventHandler() {
		this.backedEvent = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
			for (Callback<T> callback : callbacks) {
				callback.onEvent(event);
			}
		});
	}

	@Override
	public void register(Callback<T> event) {
		this.backedEvent.register(event);
	}

	@Override
	public void invoke(T event) {
		this.backedEvent.invoker().onEvent(event);
	}
}
