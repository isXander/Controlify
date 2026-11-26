/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller;

import net.minecraft.resources.Identifier;

public class SingleValueControllerComponent<T> extends ControllerComponent {
	private final T value;

	public SingleValueControllerComponent(Identifier id, T value) {
		super(id);
		this.value = value;
	}

	public T value() {
		return this.value;
	}
}
