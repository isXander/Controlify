/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/// The two broad vibration bands every controller can reproduce in some form.
public enum Band implements StringRepresentable {
	/// A heavy, low-frequency rumble. The strong motor of a dual-motor controller.
	LOW,
	/// A light, high-frequency buzz. The weak motor of a dual-motor controller.
	HIGH;

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
