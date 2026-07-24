/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.ControllerEntity;

import java.util.function.Consumer;

public class ComponentAdderDriver implements Driver {
	private final Consumer<ControllerEntity> componentAdder;

	public ComponentAdderDriver(Consumer<ControllerEntity> componentAdder) {
		this.componentAdder = componentAdder;
	}

	@Override
	public void addComponents(ControllerEntity controller) {
		componentAdder.accept(controller);
	}
}
