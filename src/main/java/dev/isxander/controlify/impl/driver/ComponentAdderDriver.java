/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver;

import dev.isxander.controlify.impl.controller.ControllerImpl;

import java.util.function.Consumer;

public class ComponentAdderDriver implements Driver {
	private final Consumer<ControllerImpl> componentAdder;

	public ComponentAdderDriver(Consumer<ControllerImpl> componentAdder) {
		this.componentAdder = componentAdder;
	}

	@Override
	public void addComponents(ControllerImpl controller) {
		componentAdder.accept(controller);
	}
}
