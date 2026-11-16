/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/// Where on the controller a feedback output is felt.
///
/// Controllers that cannot localise vibration (dual-motor rumble) play `BODY`, `LEFT` and
/// `RIGHT` through the same motors. Trigger sites are dropped on controllers without
/// trigger motors.
public enum Site implements StringRepresentable {
	/// The whole controller.
	BODY,
	/// The left grip.
	LEFT,
	/// The right grip.
	RIGHT,
	LEFT_TRIGGER,
	RIGHT_TRIGGER;

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	/// Whether this site is a trigger rather than part of the controller body.
	public boolean isTrigger() {
		return this == LEFT_TRIGGER || this == RIGHT_TRIGGER;
	}
}
