/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller;

import net.minecraft.resources.Identifier;

public abstract class ControllerComponent {
	private final Identifier id;
	private ControllerImpl controller;

	public ControllerComponent(Identifier id) {
		this.id = id;
	}

	public final Identifier id() {
		return this.id;
	}

	public void attach(ControllerImpl controller) {
		this.controller = controller;
	}

	public void close() {
	}

	protected final ControllerImpl controller() {
		return this.controller;
	}
}
