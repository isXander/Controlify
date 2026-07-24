/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.input;

import net.minecraft.resources.Identifier;

public interface ModifiableControllerState extends ControllerState {
	void setButton(Identifier button, boolean pressed);

	void setAxis(Identifier axis, float value);

	void setHat(Identifier hat, HatState state);
}
