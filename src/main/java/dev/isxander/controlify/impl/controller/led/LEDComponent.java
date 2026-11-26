/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.led;

import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

/// The controller's RGB light. Every common controller with one has exactly one: the
/// DualShock 4 and DualSense lightbars.
///
/// It starts at [#DEFAULT_COLOR], so the driver sets it as soon as the controller connects
/// and the first light effect does not appear on top of whatever the controller showed
/// before. Setting a color only stores it: the driver sends it at its own pace.
public class LEDComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("led");

	/// Off by default. SDL pre-initializes this to a dim blue or something.
	public static final int DEFAULT_COLOR = 0x000000;

	private int color = DEFAULT_COLOR;
	private boolean dirty = true;

	public LEDComponent() {
		super(ID);
	}

	/// @param color an sRGB `0xRRGGBB` color
	public void setColor(int color) {
		color &= 0xFFFFFF;
		if (this.color != color) {
			this.color = color;
			this.dirty = true;
		}
	}

	/// The sRGB `0xRRGGBB` color to show.
	public int color() {
		return this.color;
	}

	public boolean consumeDirty() {
		boolean old = this.dirty;
		this.dirty = false;
		return old;
	}
}
