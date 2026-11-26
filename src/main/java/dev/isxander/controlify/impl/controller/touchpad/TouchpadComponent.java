/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.touchpad;

import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

public class TouchpadComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("touchpad");

	private final Touchpads touchpads;

	public TouchpadComponent(Touchpads touchpads) {
		super(ID);
		this.touchpads = touchpads;
	}

	public Touchpads.Touchpad[] touchpads() {
		return touchpads.touchpads();
	}
}
