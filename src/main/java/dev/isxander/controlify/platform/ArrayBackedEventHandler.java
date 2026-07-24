/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.platform;

import java.util.ArrayList;
import java.util.List;

public class ArrayBackedEventHandler<T> implements EventHandler<T> {
	private final List<Callback<T>> callbacks = new ArrayList<>();

	@Override
	public void register(Callback<T> event) {
		this.callbacks.add(event);
	}

	@Override
	public void invoke(T event) {
		this.callbacks.forEach(c -> c.onEvent(event));
	}
}
