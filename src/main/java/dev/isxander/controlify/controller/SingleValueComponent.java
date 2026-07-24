/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller;

import net.minecraft.resources.Identifier;

public class SingleValueComponent<T> implements ECSComponent {
	private final T value;
	private final Identifier id;

	public SingleValueComponent(T value, Identifier id) {
		this.value = value;
		this.id = id;
	}

	public T value() {
		return this.value;
	}

	@Override
	public Identifier id() {
		return this.id;
	}
}
