/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.impl;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ECSComponent;

public abstract class ECSComponentImpl implements ECSComponent {
	private ControllerEntity controller;

	protected final ControllerEntity controller() {
		return this.controller;
	}

	@Override
	public void attach(ControllerEntity controller) {
		this.controller = controller;
	}
}
