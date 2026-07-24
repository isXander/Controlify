/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.platform;

public interface EventHandler<T> {
	void register(Callback<T> event);

	void invoke(T event);

	static <T> EventHandler<T> createPlatformBackedEvent() {
		return new ArrayBackedEventHandler<>();
	}

	@FunctionalInterface
	interface Callback<T> {
		void onEvent(T event);
	}
}
